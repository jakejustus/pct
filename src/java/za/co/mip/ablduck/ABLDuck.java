/**
 * Copyright 2017 MIP Holdings
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
package za.co.mip.ablduck;

import java.text.SimpleDateFormat;
import java.text.Format;
import java.text.MessageFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URL;
import java.net.URLDecoder;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.openedge.core.runtime.IPropath;
import com.openedge.core.runtime.Propath;
import com.openedge.pdt.core.ast.ASTManager;
import com.openedge.pdt.core.ast.IASTManager;
import com.openedge.pdt.core.ast.PropathASTContext;
import com.openedge.pdt.core.ast.model.IASTContext;
import com.openedge.pdt.core.ast.model.ICompilationUnit;

import com.phenix.pct.PCT;
import com.phenix.pct.Messages;
import com.phenix.pct.Version;

import za.co.mip.ablduck.models.DataJSObject;
import za.co.mip.ablduck.models.SourceJSObject;
import za.co.mip.ablduck.models.data.ClassDataObject;
import za.co.mip.ablduck.models.data.SearchDataObject;
import za.co.mip.ablduck.models.source.MemberObject;
import za.co.mip.ablduck.utilities.HTMLGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class for generating ABLDuck documentation from OpenEdge classes
 * 
 * @author <a href="mailto:robertedwardsmail@gmail.com">Robert Edwards</a>
 */
public class ABLDuck extends PCT {
    private HashMap<String, SourceJSObject> jsObjects = new HashMap<>();
    private String title = "ABLDuck documentation";
    private File destDir = null;
    private File destDirOutput = null;
    private List<FileSet> filesets = new ArrayList<>();
    protected Path propath = null;

    public ABLDuck() {
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
     * Destination directory
     * 
     * @param destFile Directory
     */
    public void setDestDir(File dir) {
        this.destDir = dir;
        this.destDirOutput = new File(dir, "output");
    }

    /**
     * Documentation title
     * 
     * @param title Documentation title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the propath to be used when running the procedure
     * 
     * @param propath an Ant Path object containing the propath
     */
    public void addPropath(Path propath) {
        createPropath().append(propath);
    }

    /**
     * Creates a new Path instance
     * 
     * @return Path
     */
    public Path createPropath() {
        if (this.propath == null) {
            this.propath = new Path(this.getProject());
        }

        return this.propath;
    }

    @Override
    public void execute() {
        checkDlcHome();

        // Destination directory must exist
        if (this.destDir == null) {
            throw new BuildException(MessageFormat.format(Messages.getString("OpenEdgeClassDocumentation.0"), "destDir"));
        }
        // There must be at least one fileset
        if (filesets.isEmpty()) {
            throw new BuildException(Messages.getString("OpenEdgeClassDocumentation.1"));
        }

        this.destDirOutput.mkdirs();

        //Extract template
        try {
            extractTemplateDirectory(this.destDir);
            
            Format formatter = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
            List<String> files = Arrays.asList("index.html", "template.html", "print-template.html");
            
            for(String file : files) {
                replaceTemplateTags("{title}", this.title, Paths.get(this.destDir.getAbsolutePath(), file));
                replaceTemplateTags("{version}", Version.getPCTVersion(), Paths.get(this.destDir.getAbsolutePath(), file));
                replaceTemplateTags("{date}", formatter.format(new Date()), Paths.get(this.destDir.getAbsolutePath(), file));
            } 
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        DataJSObject dataJSObject = new DataJSObject();
        HTMLGenerator html = new HTMLGenerator();
        IPropath pp = new Propath(new org.eclipse.core.runtime.Path(getProject().getBaseDir().getAbsolutePath()), propath.list());
        IASTContext astContext = new PropathASTContext(pp);
        IProgressMonitor monitor = new NullProgressMonitor();
        IASTManager astMgr = ASTManager.getASTManager();

        log("Generating ABLDuck documentation (11.5+ method)", Project.MSG_INFO);

        for (FileSet fs : filesets) {
            // And get files from fileset
            String[] dsfiles = fs.getDirectoryScanner(this.getProject()).getIncludedFiles();

            for (int i = 0; i < dsfiles.length; i++) {
            	File file = new File(fs.getDir(this.getProject()), dsfiles[i]);
            	log("Generating AST for " + file.getAbsolutePath(), Project.MSG_VERBOSE);

            	int extPos = file.getName().lastIndexOf('.');
                String ext = file.getName().substring(extPos);
                boolean isClass = ".cls".equalsIgnoreCase(ext);

                ICompilationUnit root = astMgr.createAST(file, astContext, monitor, IASTManager.EXPAND_ON, IASTManager.DLEVEL_FULL);
                if (isClass) {
                    ABLDuckClassVisitor visitor = new ABLDuckClassVisitor(pp, this);
                    log("Executing AST ClassVisitor " + file.getAbsolutePath(), Project.MSG_VERBOSE);
                    root.accept(visitor);
                    
                    try {
                        SourceJSObject jsObject = visitor.getJSObject();
                        jsObjects.put(jsObject.name, jsObject);
                    } catch (IOException ex) {
                        throw new BuildException(ex);
                    }
                } 
            }
        }

        //Determine class hierarchy, subclasses and search objects
        for (Map.Entry<String, SourceJSObject> j:jsObjects.entrySet()) {
            SourceJSObject js = j.getValue();

            //Class tree
            ClassDataObject cls = new ClassDataObject();
            cls.name = js.name;
            cls.ext = js.ext;
            cls.icon = "icon-class";

            if (js.meta.isPrivate != null && js.meta.isPrivate)
                cls.isPrivate = true;

            dataJSObject.classes.add(cls);

            //Create search object
            SearchDataObject search = new SearchDataObject();
            search.name = js.shortname;
            search.icon = "icon-class";
            search.url = "#!/api/" + js.name;
            search.sort = 1;
            search.meta = js.meta;

            dataJSObject.search.add(search);

            for (MemberObject member : js.members) {
                search = new SearchDataObject();
                search.name = member.name;
                search.icon = "icon-" + member.tagname;
                search.url = "#!/api/" + member.owner + "-method-" + member.name;
                search.sort = 3;
                search.meta = member.meta;
    
                dataJSObject.search.add(search);
            }
            
            //Hierarchy
            HierarchyResult result = new HierarchyResult();
            result = determineClassHierarchy(js, result);

            List<String> hierarchy = result.getHierarchy();
            Collections.reverse(hierarchy);
            js.superclasses.addAll(hierarchy);

            js.members.addAll(result.getInheritedmembers());

            //Subclasses
            for (Map.Entry<String, SourceJSObject> subclass:jsObjects.entrySet()) {
                SourceJSObject subc = subclass.getValue();
                if(subc.ext.equals(js.name)) 
                    js.subclasses.add(subc.name);
            }

        }

        //Write class js files out
        for (Map.Entry<String, SourceJSObject> j:jsObjects.entrySet()) {
            SourceJSObject js = j.getValue();

            //Generate html
            js.html = html.getClassHtml(jsObjects, js);
    
            File outputFile = new File(this.destDirOutput, js.name + ".js");
            try (FileWriter file = new FileWriter(outputFile.toString())) {
                file.write("Ext.data.JsonP." + js.name.replace(".", "_") + "(" + gson.toJson(js) + ");");
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }

        File dataFile = new File(this.destDir, "data.js");
        try (FileWriter file = new FileWriter(dataFile.toString())) {
            file.write("Docs = {\"data\":" + gson.toJson(dataJSObject) + "}");
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
    }

    private HierarchyResult determineClassHierarchy(SourceJSObject curClass, HierarchyResult result) {
        
        String inherits = curClass.ext;

        if (!"".equals(inherits)) {
            result.addHierarchy(inherits);

            SourceJSObject nextClass = jsObjects.get(inherits);
                        
            if (nextClass != null) {
                for (MemberObject member : nextClass.members) {
                    if (member.owner.equals(inherits) && (member.meta.isPrivate == null || !member.meta.isPrivate)) 
                        result.addInheritedmember(member);
                }
            
                determineClassHierarchy(nextClass, result);
            }
        }
        return result;
    }

    private void extractTemplateDirectory(File outputDir) throws IOException {
        URL url = getClass().getClassLoader().getResource(getClass().getName().replace(".", "/") + ".class");
        String jarPath = url.getPath().substring(5, url.getPath().indexOf('!'));

        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("ablducktemplate")) {
                    File template = new File(outputDir, entry.getName().substring(15));
                    if (entry.isDirectory()) {
                        template.mkdirs();
                        continue;
                    }

                    copyStreamFromJar("/" + entry.getName(), template);
                }
            }
        }
    }

    private void replaceTemplateTags(String tag, String value, java.nio.file.Path file) throws IOException {
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(file), charset);
        content = content.replaceAll(Pattern.quote(tag), Matcher.quoteReplacement(value));
        Files.write(file, content.getBytes(charset));
    }
}
