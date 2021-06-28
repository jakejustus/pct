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
package com.phenix.pct;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

/**
 * Class for compiling Progress procedures
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET </a>
 */
public class PCTBgCompile extends PCTBgRun {
    protected CompilationAttributes compAttrs;
    private Mapper mapperElement;

    protected SortedSet<CompilationUnit> units = new TreeSet<>();

    private int compOk = 0;
    private int compNotOk = 0;
    private int compSkipped = 0;
    // Internal use
    private int compId = -1;
    private File compDir = null;

    public PCTBgCompile() {
        super();
        compAttrs = new CompilationAttributes(this);

        compId = PCT.nextRandomInt();
        compDir = new File(System.getProperty("java.io.tmpdir"), "pctcomp" + compId);
    }

    /**
     * Should only be accessed from CompilationWrapper
     */
    protected void setCompilationAttributes(CompilationAttributes attrs) {
        this.compAttrs = attrs;
    }

    protected void setMapper(Mapper mapper) {
        this.mapperElement = mapper;
    }

    private synchronized void addCompilationCounters(int ok, int notOk, int skipped) {
        compOk += ok;
        compNotOk += notOk;
        compSkipped += skipped;
    }

    @Override
    public void setProcedure(String procedure) {
        throw new BuildException("Can't set procedure attribute");
    }

    /**
     * Do the work
     * 
     * @throws BuildException Something went wrong
     */
    @Override
    public void execute() {
        if (compDir.exists() || !compDir.mkdirs()) {
            this.cleanup();
            throw new BuildException("Unable to create temp directory for compile procedure");
        }

        // Test output directory
        if (compAttrs.getDestDir() != null) {
            if (compAttrs.getDestDir().exists()) {
                if (!compAttrs.getDestDir().isDirectory()) {
                    this.cleanup();
                    throw new BuildException(Messages.getString("PCTCompile.35")); //$NON-NLS-1$
                }
            } else {
                if (!compAttrs.getDestDir().mkdirs()) {
                    this.cleanup();
                    throw new BuildException(Messages.getString("PCTCompile.36")); //$NON-NLS-1$
                }
            }
        }

        // Test xRef directory
        if (compAttrs.getxRefDir() == null) {
            compAttrs.setXRefDir(new File(compAttrs.getDestDir(), ".pct")); //$NON-NLS-1$
        }

        if (compAttrs.getxRefDir().exists()) {
            if (!compAttrs.getxRefDir().isDirectory()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.38")); //$NON-NLS-1$
            }
        } else {
            if (!compAttrs.getxRefDir().mkdirs()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.39")); //$NON-NLS-1$
            }
        }

        log(Messages.getString("PCTCompile.40"), Project.MSG_INFO); //$NON-NLS-1$

        // Checking xcode and (listing || preprocess) attributes -- They're mutually exclusive
        if (compAttrs.isXcode() && (compAttrs.isListing() || compAttrs.isPreprocess())) {
            log(Messages.getString("PCTCompile.43"), Project.MSG_INFO); //$NON-NLS-1$
        }

        if (!compAttrs.isXcode() && (getOptions().getXCodeSessionKey() != null) && !getOptions().getXCodeSessionKey().trim().isEmpty()) {
            log("xcode attribute set to false, resetting xcodeSessionKey attribute");
            getOptions().setXCodeSessionKey(null);
        }

        // Verify resource collections
        for (ResourceCollection rc : compAttrs.getResources()) {
            if (!rc.isFilesystemOnly()) {
                cleanup();
                throw new BuildException(
                        "PCTCompile only supports file-system resources collections");
            }
        }

        // Display warning message if xmlXref and stringXref used at the same time
        if (compAttrs.isXmlXref() && compAttrs.isStringXref()) {
            log(Messages.getString("PCTCompile.92"), Project.MSG_WARN); //$NON-NLS-1$
        }

        List<String> outputTypes = compAttrs.getOutputType();
        if (outputTypes.contains(CompilationAttributes.JSON_OUTPUT_TYPE)) {
            log(Messages.getString("PCTCompile.93"), Project.MSG_WARN);
            outputTypes.remove(CompilationAttributes.JSON_OUTPUT_TYPE);
        }

        initializeCompilationUnits();
        compAttrs.writeCompilationProcedure(new File(compDir, "pctcomp.p"), getCharset());
        getOptions().addPropath(new Path(getProject(), compDir.getAbsolutePath()));

        try {
            super.execute();
        } finally {
            log(MessageFormat.format(Messages.getString("PCTCompile.44"), Integer //$NON-NLS-1$
                    .valueOf(compOk - compSkipped)));
            if (compNotOk > 0) {
                log(MessageFormat.format(Messages.getString("PCTCompile.45"), Integer //$NON-NLS-1$
                        .valueOf(compNotOk)));
            }
        }
    }

    /**
     * Generates a list of compilation unit (which is a source file name, associated with output
     * file names (.r, XREF, listing, and so on). This list is then consumed by the background
     * workers and transmitted to the OpenEdge procedures.
     */
    private void initializeCompilationUnits() {
        int zz = 0;
        for (ResourceCollection rc : compAttrs.getResources()) {
            for (Resource r : rc) {
                FileResource frs = (FileResource) r;
                if (!frs.isDirectory()) {
                    CompilationUnit unit = new CompilationUnit();
                    unit.id = zz++;
                    unit.fsRootDir = getResourceBaseDir(frs.getBaseDir());
                    unit.fsFile = frs.getName();
                    unit.targetFile = mapperElement == null
                            ? null
                            : mapperElement.getImplementation().mapFileName(frs.getName())[0];
                    units.add(unit);
                }
            }
        }
    }

    private String getResourceBaseDir(File baseDir) {
        String resBaseDir;
        if (getOptions().useRelativePaths()) {
            if (!isDirInPropath(baseDir))
                log(MessageFormat.format(Messages.getString("PCTCompile.48"),
                        baseDir.getAbsolutePath()), Project.MSG_WARN);
            try {
                resBaseDir = FileUtils
                        .getRelativePath(
                                getOptions().getBaseDir() == null
                                        ? getProject().getBaseDir()
                                        : getOptions().getBaseDir(),
                                baseDir)
                        .replace('/', File.separatorChar);
            } catch (Exception caught) {
                throw new BuildException(caught);
            }
        } else {
            resBaseDir = baseDir.getAbsolutePath(); // $NON-NLS-1$
        }
        return resBaseDir;
    }

    private boolean isDirInPropath(File dir) {
        if (getOptions().getPropath() == null)
            return false;
        for (String str : getOptions().getPropath().list()) {
            if (new File(str).equals(dir))
                return true;
        }
        return false;
    }

    protected BackgroundWorker createOpenEdgeWorker(Socket socket) {
        CompilationBackgroundWorker worker = new CompilationBackgroundWorker(this);
        try {
            worker.initialize(socket);
        } catch (Exception uncaught) {
            throw new BuildException(uncaught);
        }

        return worker;
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        
        if (getOptions().isDebugPCT())
            return;
        deleteFile(compDir);
    }

    public class CompilationBackgroundWorker extends BackgroundWorker {
        protected int customStatus = 0;
        
        public CompilationBackgroundWorker(PCTBgCompile parent) {
            super(parent);
        }

        @Override
        protected boolean performCustomAction() throws IOException {
            if (customStatus == 0) {
                customStatus = 3;
                sendCommand("launch", "pct/pctBgCompile.p");
                return true;
            } else if (customStatus == 3) {
                customStatus = 4;
                sendCommand("setOptions", getOptions());
                return true;
            } else if (customStatus == 4) {
                List<CompilationUnit> sending = new ArrayList<>();
                boolean noMoreFiles = false;
                synchronized (units) {
                    int size = units.size();
                    if (size > 0) {
                        int numCU = (size > 100 ? 10 : 1);
                        Iterator<CompilationUnit> iter = units.iterator();
                        for (int zz = 0; zz < numCU; zz++) {
                            sending.add(iter.next());
                        }
                        for (Iterator<CompilationUnit> iter2 = sending.iterator(); iter2.hasNext();) {
                            units.remove(iter2.next());
                        }
                    } else {
                        noMoreFiles = true;
                    }
                }
                
                StringBuilder sb = new StringBuilder();
                if (noMoreFiles) {
                    return false;
                } else {
                    for (Iterator<CompilationUnit> iter = sending.iterator(); iter.hasNext();) {
                        CompilationUnit cu = iter.next();
                        if (sb.length() > 0)
                            sb.append('*');
                        sb.append(cu.toString());
                    }
                    sendCommand("PctCompile", sb.toString());
                    return true;
                }
            } else {
                return false;
            }
        }
    
        @Override
        public void setCustomOptions(Map<String, String> options) {
            // No-op
        }

        protected String getOptions() {

            StringBuilder sb = new StringBuilder();
            sb.append(Boolean.toString(compAttrs.isRunList())).append(';');
            sb.append("").append(';'); // Previously min-size
            sb.append("").append(';'); // Previously MD5
            sb.append(Boolean.toString(compAttrs.isXcode())).append(';');
            sb.append("").append(';');
            sb.append(Boolean.toString(compAttrs.isForceCompile())).append(';');
            sb.append(Boolean.toString(false /* FIXME noCompile */)).append(';');
            sb.append(Boolean.toString(compAttrs.isKeepXref())).append(';');
            sb.append("").append(';'); // Previously languages
            sb.append("").append(';'); // Previously growth factor
            sb.append(Boolean.toString(compAttrs.isMultiCompile())).append(';');
            sb.append("").append(';'); // Previously stream-io
            sb.append("").append(';'); // Previous v6frame
            sb.append(Boolean.toString(PCTBgCompile.this.getOptions().useRelativePaths())).append(';');
            sb.append(compAttrs.getDestDir() == null ? "" : compAttrs.getDestDir().getAbsolutePath()).append(';');
            sb.append(Boolean.toString(compAttrs.isPreprocess())).append(';');
            sb.append(compAttrs.getPreprocessDir() == null ? "" : compAttrs.getPreprocessDir().getAbsolutePath()).append(';');
            sb.append(Boolean.toString(compAttrs.isListing())).append(';');
            sb.append(Boolean.toString(compAttrs.isDebugListing())).append(';');
            sb.append(compAttrs.getDebugListingDir() == null ? "" : compAttrs.getDebugListingDir().getAbsolutePath()).append(';');
            sb.append(compAttrs.getIgnoredIncludes()).append(';');
            sb.append(Boolean.toString(compAttrs.isXmlXref())).append(';');
            sb.append(Boolean.toString(compAttrs.isStringXref())).append(';');
            sb.append(Boolean.toString(compAttrs.isAppendStringXref())).append(';');
            sb.append("").append(';'); // Previously saveR
            sb.append(compAttrs.getListingSource()).append(';');
            sb.append(Boolean.toString(compAttrs.isNoParse())).append(';');
            sb.append(Boolean.toString(compAttrs.isStopOnError())).append(';');
            sb.append(Boolean.toString(compAttrs.isFlattenDbg())).append(';');
            sb.append(compAttrs.getxRefDir().getAbsolutePath()).append(';');
            sb.append(Integer.toString(compAttrs.getFileList())).append(';');
            sb.append(Boolean.toString(compAttrs.isRequireFullKeywords())).append(';');
            sb.append(Boolean.toString(compAttrs.isRequireFullNames())).append(';');
            sb.append(Boolean.toString(compAttrs.isRequireFieldQualifiers())).append(';');
            sb.append(compAttrs.getCallbackClass() == null ? "" : compAttrs.getCallbackClass()).append(';');
            sb.append(CompilationAttributes.CONSOLE_OUTPUT_TYPE).append(';');
            sb.append(Boolean.toString(compAttrs.isRequireReturnValues())).append(';');

            return sb.toString();
        }

        @Override
        public void handleResponse(String command, String parameter, boolean err,
                String customResponse, List<Message> returnValues) {
            if ("pctCompile".equalsIgnoreCase(command)) {
                String[] str = customResponse.split("/");
                int ok = 0;
                int notOk = 0;
                int skipped = 0;
                try {
                    ok = Integer.parseInt(str[0]);
                    notOk = Integer.parseInt(str[1]);
                    skipped = Integer.parseInt(str[2]);
                } catch (NumberFormatException nfe) {
                    throw new BuildException("Invalid response from command " + command + "(" + parameter + ") : '" 
                            + customResponse + "'", nfe);
                }
                addCompilationCounters(ok, notOk, skipped);
                logMessages(returnValues);
                if (err) {
                    if (PCTBgCompile.this.getOptions().isFailOnError())
                        setBuildException(new BuildException(command + "(" + parameter + ") : " + customResponse));
                    if (compAttrs.isStopOnError())
                        setStatusQuit();
                }
            }
        }
    }

    static class CompilationUnit implements Comparable<CompilationUnit> {
        private int id;
        private String fsRootDir; // Fileset root directory
        private String fsFile;    // Fileset relative file name
        private String targetFile;
        
        public String getFsFile() {
            return fsFile;
        }

        public String getTargetFile() {
            return targetFile;
        }
        
        /**
         * Return the name of the rcode
         * @return
         */
        public String getRcodeFile(){
            if (getTargetFile() != null) {
                return getTargetFile();
            }
            else {
                String src = getFsFile();
                int index = src.lastIndexOf('.');
                return src.substring(0, index > 0 ? index : src.length()) + ".r";
            }
        }
        
        public boolean isClass () {
            return fsFile.toLowerCase().endsWith(".cls");
        }


        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (this.getClass() == obj.getClass()) {
                CompilationUnit other = (CompilationUnit) obj;
                return id == other.id;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return fsRootDir + "|" + fsFile + "|" + (targetFile == null ? "" : targetFile);
        }

        @Override
        public int compareTo(CompilationUnit o) {
            return id - o.id;
        }
    }

}
