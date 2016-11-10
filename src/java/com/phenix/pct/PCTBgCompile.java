/**
 * Copyright 2005-2016 Riverside Software
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class for compiling Progress procedures
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET </a>
 */
public class PCTBgCompile extends PCTBgRun {
    private CompilationAttributes compAttrs;
    private Mapper mapperElement;

    private SortedSet<CompilationUnit> units = new TreeSet<CompilationUnit>();

    private int compOk = 0;
    private int compNotOk = 0;
    private int compSkipped = 0;

    public PCTBgCompile() {
        super();
        compAttrs = new CompilationAttributes(this);
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
    public void execute() throws BuildException {
        if (compAttrs.getDestDir() == null) {
            this.cleanup();
            throw new BuildException(Messages.getString("PCTCompile.34")); //$NON-NLS-1$
        }

        // Test output directory
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
            log(Messages.getString("PCTCompile.43"), Project.MSG_INFO); //$NON-NLS-1$ // TODO Update this message
        }

        initializeCompilationUnits();

        try {
            super.execute();
        } finally {
            log(MessageFormat.format(Messages.getString("PCTCompile.44"), new Object[]{Integer //$NON-NLS-1$
                    .valueOf(compOk - compSkipped)}));
            if (compNotOk > 0) {
                log(MessageFormat.format(Messages.getString("PCTCompile.45"), new Object[]{Integer //$NON-NLS-1$
                        .valueOf(compNotOk)}));
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
                unit.fsRootDir = frs.getBaseDir();
                unit.fsFile = frs.getName();
                unit.targetFile = mapperElement == null ? null : mapperElement.getImplementation().mapFileName(frs.getName())[0];
                units.add(unit);
                }
            }
//            for (String str : fs.getDirectoryScanner(getProject()).getIncludedFiles()) {
//                CompilationUnit unit = new CompilationUnit();
//                unit.fsRootDir = fs.getDir(getProject());
//                unit.fsFile = str;
//                unit.targetFile = getMapper() == null ? null : getMapper().mapFileName(str)[0];
//                units.add(unit);
//            }
        }

    }

    protected BackgroundWorker createOpenEdgeWorker(Socket socket) {
        CompilationBackgroundWorker worker = new CompilationBackgroundWorker(this);
        try {
            worker.initialize(socket);
        } catch (Throwable uncaught) {
            throw new BuildException(uncaught);
        }

        return worker;
    }

    public class CompilationBackgroundWorker extends BackgroundWorker {
        private int customStatus = 0;

        public CompilationBackgroundWorker(PCTBgCompile parent) {
            super(parent);
        }

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
                List<CompilationUnit> sending = new ArrayList<CompilationUnit>();
                boolean noMoreFiles = false;
                synchronized (units) {
                    int size = units.size();
                    if (size > 0) {
                        int numCU = (size > 100 ? 10 : 1);
                        Iterator<CompilationUnit> iter = units.iterator();
                        for (int zz = 0; zz < numCU; zz++) {
                            sending.add((CompilationUnit) iter.next());
                        }
                        for (Iterator<CompilationUnit> iter2 = sending.iterator(); iter2.hasNext();) {
                            units.remove((CompilationUnit) iter2.next());
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

        }

        private String getOptions() {
            StringBuilder sb = new StringBuilder();
            sb.append(Boolean.toString(compAttrs.isRunList())).append(';');
            sb.append(Boolean.toString(compAttrs.isMinSize())).append(';');
            sb.append(Boolean.toString(compAttrs.isMd5())).append(';');
            sb.append(Boolean.toString(compAttrs.isXcode())).append(';');
            sb.append(compAttrs.getXcodeKey() == null ? "" : compAttrs.getXcodeKey()).append(';');
            sb.append(Boolean.toString(compAttrs.isForceCompile())).append(';');
            sb.append(Boolean.toString(false /* FIXME noCompile */)).append(';');
            sb.append(Boolean.toString(compAttrs.isKeepXref())).append(';');
            sb.append(compAttrs.getLanguages() == null ? "" : compAttrs.getLanguages()).append(';');
            sb.append(Integer.toString(compAttrs.getGrowthFactor() > 0 ? compAttrs.getGrowthFactor() : 100)).append(';');
            sb.append(Boolean.toString(compAttrs.isMultiCompile())).append(';');
            sb.append(Boolean.toString(compAttrs.isStreamIO())).append(';');
            sb.append(Boolean.toString(compAttrs.isV6Frame())).append(';');
            sb.append(Boolean.toString(PCTBgCompile.this.getOptions().useRelativePaths())).append(';');
            sb.append(compAttrs.getDestDir().getAbsolutePath()).append(';');
            sb.append(Boolean.toString(compAttrs.isPreprocess())).append(';');
            sb.append(compAttrs.getPreprocessDir() == null ? "" : compAttrs.getPreprocessDir().getAbsolutePath()).append(';');
            sb.append(Boolean.toString(compAttrs.isListing())).append(';');
            sb.append(Boolean.toString(compAttrs.isDebugListing())).append(';');
            sb.append(compAttrs.getDebugListingDir() == null ? "" : compAttrs.getDebugListingDir().getAbsolutePath()).append(';');
            sb.append(compAttrs.getIgnoredIncludes()).append(';');
            sb.append(Boolean.toString(compAttrs.isXmlXref())).append(';');
            sb.append(Boolean.toString(compAttrs.isStringXref())).append(';');
            sb.append(Boolean.toString(compAttrs.isAppendStringXref())).append(';');
            sb.append(Boolean.toString(compAttrs.isSaveR())).append(';');
            sb.append(compAttrs.getListingSource()).append(';');
            sb.append(Boolean.toString(compAttrs.isNoParse())).append(';');
            sb.append(Boolean.toString(compAttrs.isStopOnError())).append(';');
            sb.append(Boolean.toString(compAttrs.isFlattenDbg())).append(';');
            sb.append(compAttrs.getxRefDir().getAbsolutePath()).append(';');
            sb.append(Integer.toString(compAttrs.getFileList())).append(';');

            return sb.toString();
        }

        @Override
        public void handleResponse(String command, String parameter, boolean err,
                String customResponse, List<Message> returnValues) {
            if ("pctCompile".equalsIgnoreCase(command)) {
                String[] str = customResponse.split("/");
                int ok = 0, notOk = 0, skipped = 0;
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
                        quit();
                }
            }
        }
    }

    private static class CompilationUnit implements Comparable<CompilationUnit> {
        private int id;
        private File fsRootDir; // Fileset root directory
        private String fsFile; // Fileset relative file name
        private String targetFile;

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (obj instanceof CompilationUnit) {
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
