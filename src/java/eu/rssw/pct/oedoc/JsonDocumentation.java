/**
 * Copyright 2005-2021 Riverside Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package eu.rssw.pct.oedoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Variable;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.stream.JsonWriter;
import com.phenix.pct.DBConnectionSet;
import com.phenix.pct.PCT;
import com.phenix.pct.PCTAlias;
import com.phenix.pct.PCTConnection;
import com.phenix.pct.PCTDumpSchema;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;

/**
 * Generate JSON documentation from OpenEdge classes
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET </a>
 */
public class JsonDocumentation extends PCT {
    private File destDir = null;
    private File buildDir = null;
    private String encoding = null;
    private List<FileSet> filesets = new ArrayList<>();
    private Path propath = null;
    private Collection<PCTConnection> dbConnList = null;
    private Collection<DBConnectionSet> dbConnSet = null;
    private boolean indent = false;
    private CommentStyle style = CommentStyle.JAVADOC;

    public JsonDocumentation() {
        super();
        createPropath();
    }

    /**
     * Adds a set of files to archive.
     * 
     * @param set FileSet
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    /**
     * RCode directory
     */
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    /**
     * Destination directory
     */
    public void setDestDir(File dir) {
        this.destDir = dir;
    }

    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    public void setStyle(String style) {
        this.style = CommentStyle.valueOf(style.toUpperCase());
    }

    /**
     * Codepage to use when reading files
     * 
     * @param encoding String
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set the propath to be used when parsing source code
     * 
     * @param propath an Ant Path object containing the propath
     */
    public void addPropath(Path propath) {
        createPropath().append(propath);
    }

    public void addDBConnection(PCTConnection dbConn) {
        if (dbConnList == null) {
            dbConnList = new ArrayList<>();
        }
        dbConnList.add(dbConn);
    }

    public void addDBConnectionSet(DBConnectionSet set) {
        if (this.dbConnSet == null) {
            this.dbConnSet = new ArrayList<>();
        }
        dbConnSet.add(set);
    }

    /**
     * Creates a new Path instance
     * 
     * @return Path
     */
    private Path createPropath() {
        if (this.propath == null) {
            this.propath = new Path(this.getProject());
        }

        return this.propath;
    }

    /**
     * Do the work
     * 
     * @throws BuildException Something went wrong
     */
    @Override
    public void execute() {
        checkDlcHome();

        // Destination directory must exist
        if (this.destDir == null) {
            throw new BuildException("destDir attribute is not set");
        }
        if (!createDir(destDir)) {
            throw new BuildException("Unable to create destination directory");
        }

        // There must be at least one fileset
        if (filesets.isEmpty()) {
            throw new BuildException("At least one fileset should be defined");
        }

        ProparseSettings ppSettings;
        RefactorSession session;
        try {
            String pp = Joiner.on(',').join(propath.list());
            log("Using PROPATH: " + pp, Project.MSG_INFO);
            ppSettings = new ProparseSettings(pp, false);
            session = new RefactorSession(ppSettings, readDBSchema(), Charset.forName(encoding));

            // Multi-threaded pool
            AtomicInteger numRCode = new AtomicInteger(0);
            ExecutorService service = Executors.newFixedThreadPool(4);
            Files.fileTraverser().depthFirstPreOrder(buildDir).forEach(f -> {
                if (f.getName().endsWith(".r")) {
                    numRCode.incrementAndGet();
                    service.submit(() -> {
                        ITypeInfo info = parseRCode(f);
                        if (info != null) {
                            session.injectTypeInfo(info);
                        }
                    });
                }
            });
            service.shutdown();
        } catch (IOException caught) {
            throw new BuildException(caught);
        }

        File outFile = new File(destDir, "out.json");
        log("Generating JSON documentation in " + outFile.getAbsolutePath(), Project.MSG_INFO);
        try (Writer fw = new FileWriter(outFile); JsonWriter writer = new JsonWriter(fw)) {
            if (indent)
                writer.setIndent("  ");
            writer.beginArray();

            for (FileSet fs : filesets) {
                String[] dsfiles = fs.getDirectoryScanner(this.getProject()).getIncludedFiles();
                for (int i = 0; i < dsfiles.length; i++) {
                    File file = new File(fs.getDir(this.getProject()), dsfiles[i]);
                    log("ProParse: " + dsfiles[i], Project.MSG_DEBUG);
                    ParseUnit unit = new ParseUnit(file, dsfiles[i], session);
                    unit.treeParser01();

                    if (session.getTypeInfo(unit.getClassName()) != null)
                        writeClass(writer, session.getTypeInfo(unit.getClassName()), unit);
                    else
                        writeProcedure(dsfiles[i], writer, unit);
                }
            }
            writer.endArray();
        } catch (IOException caught) {
            throw new BuildException(caught);
        }
    }

    private void writeProcedure(String name, JsonWriter ofile, ParseUnit unit)
            throws IOException {
        ofile.beginObject();
        ofile.name("name").value(name);
        ofile.endObject();
    }

    private void writeClass(JsonWriter ofile, ITypeInfo info, ParseUnit unit)
            throws IOException {
        ofile.beginObject();
        ofile.name("className").value(info.getTypeName());
        ofile.name("inherits").value(info.getParentTypeName());
        ofile.name("abstract").value(info.isAbstract());
        ofile.name("final").value(info.isFinal());
        ofile.name("interface").value(info.isInterface());
        ofile.name("serializable").value(info.isSerializable());
        ofile.name("enum").value(unit.isEnum());
        ofile.name("interfaces").beginArray();
        for (String str : info.getInterfaces()) {
            ofile.value(str);
        }
        ofile.endArray();

        List<String> classComments = getJavadoc(info, unit);
        writeClassComments(ofile, info, unit, classComments);

        ofile.name("methods").beginArray();
        for (IMethodElement methd : info.getMethods()) {
            if (!methd.isConstructor() && !methd.isDestructor())
                writeMethod(ofile, methd, unit);
        }
        ofile.endArray();

        ofile.name("constructors").beginArray();
        for (IMethodElement methd : info.getMethods()) {
            if (methd.isConstructor())
                writeMethod(ofile, methd, unit);
        }
        ofile.endArray();

        ofile.name("destructors").beginArray();
        for (IMethodElement methd : info.getMethods()) {
            if (methd.isDestructor())
                writeMethod(ofile, methd, unit);
        }
        ofile.endArray();

        ofile.name("properties").beginArray();
        for (IPropertyElement prop : info.getProperties()) {
            ofile.beginObject();
            ofile.name("name").value(prop.getName());

            List<String> propComments = getJavadoc(prop, unit);
            writePropertyComments(ofile, prop, unit, propComments);

            ofile.endObject();
        }
        ofile.endArray();

        ofile.endObject();
    }

    private void writeMethod(JsonWriter ofile, IMethodElement methd, ParseUnit unit)
            throws IOException {
        ofile.beginObject();
        ofile.name("name").value(methd.getName());
        ofile.name("returnType").value(methd.getReturnTypeName());
        ofile.name("abstract").value(methd.isAbstract());
        ofile.name("static").value(methd.isStatic());
        ofile.name("modifier").value(
                methd.isPublic() ? "public" : (methd.isProtected() ? "protected" : "private"));

        List<String> comments = getJavadoc(methd, unit);
        writeMethodComments(ofile, methd, unit, comments);

        ofile.name("parameters").beginArray();
        for (IParameter prm : methd.getParameters()) {
            ofile.beginObject();
            ofile.name("modifier").value(prm.getMode().toString());
            ofile.name("type").value(prm.getDataType());
            ofile.endObject();
        }
        ofile.endArray();
        ofile.endObject();
    }

    private void writeClassComments(JsonWriter ofile, ITypeInfo info, ParseUnit unit,
            List<String> comments) throws IOException {
        ofile.name("comments").beginArray();
        for (String str : comments) {
            ofile.value(str);
        }
        ofile.endArray();
    }

    private void writeMethodComments(JsonWriter ofile, IMethodElement info, ParseUnit unit,
            List<String> comments) throws IOException {
        ofile.name("comments").beginArray();
        for (String str : comments) {
            ofile.value(str);
        }
        ofile.endArray();
    }

    private void writePropertyComments(JsonWriter ofile, IPropertyElement info,
            ParseUnit unit, List<String> comments) throws IOException {
        ofile.name("comments").beginArray();
        for (String str : comments) {
            ofile.value(str);
        }
        ofile.endArray();
    }

    private List<String> getJavadoc(ITypeInfo info, ParseUnit unit) {
        JPNode clsNode = unit.getTopNode().queryStateHead(ABLNodeType.CLASS).stream().findFirst()
                .orElse(null);
        if (clsNode != null) {
            return getJavadoc(clsNode);
        } else
            return new ArrayList<>();

    }

    private List<String> getJavadoc(IMethodElement elem, ParseUnit unit) {
        Routine r = unit.getRootScope().getRoutineMap().get(elem.getName().toLowerCase());
        if (r == null)
            return new ArrayList<>();
        else
            return getJavadoc(r.getDefineNode().getStatement());

    }

    private List<String> getJavadoc(IPropertyElement elem, ParseUnit unit) {
        Variable v = unit.getRootScope().getVariable(elem.getName());
        if (v == null)
            return new ArrayList<>();
        else
            return getJavadoc(v.getDefineNode().getStatement());
    }

    private List<String> getJavadoc(JPNode stmt) {
        // Read comments before the statement and its annotations
        List<String> comments = new ArrayList<>();
        for (ProToken tok : stmt.getHiddenTokens()) {
            if (tok.getNodeType() == ABLNodeType.COMMENT)
                comments.add(tok.getText());
        }
        stmt = stmt.getPreviousNode();
        while (stmt.getNodeType() == ABLNodeType.ANNOTATION) {
            for (ProToken tok : stmt.getHiddenTokens()) {
                if (tok.getNodeType() == ABLNodeType.COMMENT)
                    comments.add(tok.getText());
            }
            stmt = stmt.getPreviousNode();
        }
        
        return convertJavadoc(comments);
    }

    private List<String> convertJavadoc(List<String> comments) {
        List<String> rslt = new ArrayList<>();
        for (String s : comments) {
            rslt.addAll(convertJavadoc(s));
        }
        return rslt;
    }

    private List<String> convertJavadoc(String comment) {
        List<String> rslt = new ArrayList<>();
        if (checkStartComment(comment.trim())) {
            for (String s : Splitter.on('\n').split(comment.trim())) {
                // First line and last line is not supposed to contain anything
                if (!checkStartComment(s.trim()) && !s.endsWith("*/")) {
                    // Trim first * 
                    if (s.trim().startsWith("*"))
                        rslt.add(s.trim().substring(2).trim());
                        else rslt.add(s.trim());
                }
            }
        }
        return rslt;
    }

    private boolean checkStartComment(String comment) {
        if (style == CommentStyle.JAVADOC) {
            return comment.startsWith("/**");
        } else if (style == CommentStyle.SIMPLE) {
            return comment.startsWith("/*");
        } else if (style == CommentStyle.CONSULTINGWERK) {
            return comment.startsWith("/*-");
        }
        return false;
    }

    private ITypeInfo parseRCode(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            log("Parsing rcode: " + file.getAbsolutePath(), Project.MSG_DEBUG);
            RCodeInfo rci = new RCodeInfo(fis);
            if (rci.isClass()) {
                return rci.getTypeInfo();
            }
        } catch (InvalidRCodeException | IOException | RuntimeException caught) {
            log("Unable to parse rcode " + file.getAbsolutePath()
                    + " - Please open issue on GitHub - " + caught.getClass().getName(),
                    Project.MSG_ERR);
        }
        return null;
    }

    private Schema readDBSchema() throws IOException {
        Collection<PCTConnection> connList = getDBConnections();
        IDatabase[] dbs = new IDatabase[connList.size()];
        int cnt = 0;

        for (PCTConnection conn : connList) {
            log("Dumping schema for database #" + cnt + " - " + conn.getDbName(), Project.MSG_INFO);
            File outFile = dumpSchema(conn);
            dbs[cnt++] = new DatabaseWrapper(
                    DumpFileUtils.getDatabaseDescription(outFile, conn.getDbName()));
        }

        Schema schema = new Schema(dbs);
        schema.injectMetaSchema();
        for (PCTConnection conn : connList) {
            for (PCTAlias alias : conn.getAliases()) {
                schema.createAlias(alias.getName(), conn.getDbName());
            }
        }
        if (!schema.getDbSet().isEmpty())
            schema.createAlias("dictdb", schema.getDbSet().first().getName());

        return schema;
    }

    private Collection<PCTConnection> getDBConnections() {
        Collection<PCTConnection> dbs = new ArrayList<>();
        if (dbConnList != null) {
            dbs.addAll(dbConnList);
        }
        if (dbConnSet != null) {
            for (DBConnectionSet set : dbConnSet) {
                dbs.addAll(set.getDBConnections());
            }
        }
        return dbs;
    }

    private File dumpSchema(PCTConnection conn) {
        File outFile = null;
        try {
            File.createTempFile("jsondocsch", ".df");
        } catch (IOException caught) {
            throw new BuildException(caught);
        }
        PCTDumpSchema run = new PCTDumpSchema();
        run.bindToOwner(this);
        run.setDlcHome(getDlcHome());
        run.setCpStream("utf-8");
        run.setDestFile(outFile);
        run.addDBConnection(conn);
        run.execute();

        return outFile;
    }

    public enum CommentStyle {
        JAVADOC, // /**
        SIMPLE, // /*
        CONSULTINGWERK, // /*-
    }
}
