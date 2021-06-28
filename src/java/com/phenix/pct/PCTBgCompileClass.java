/**
 * Copyright 2005-2020 Riverside Software
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

import com.phenix.pct.PCTBgCompile.CompilationBackgroundWorker;

/**
 * Class for compiling Progress procedures
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET </a>
 */
public class PCTBgCompileClass extends PCTBgCompile {
    private static final String TEMPCOMPDIR = ".pctcomp" ;
    
    
    public PCTBgCompileClass() {
        super();
        
    }

    @Override
    protected BackgroundWorker createOpenEdgeWorker(Socket socket) {
        CompilationClassBackgroundWorker worker = new CompilationClassBackgroundWorker(this);
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
        // Suppression du répertoire temporaire
        deleteDirectory(Paths.get(compAttrs.getDestDir() == null ? "" : compAttrs.getDestDir().getAbsolutePath(), TEMPCOMPDIR).toFile());
    }

    public class CompilationClassBackgroundWorker extends CompilationBackgroundWorker {
        private java.nio.file.Path tempPath;
        List<CompilationUnit> myUnits = new ArrayList<>();
        
        public CompilationClassBackgroundWorker(PCTBgCompile parent) {
            super(parent);
        }

        @Override
        protected boolean performCustomAction() throws IOException {
            if (customStatus == 4) {
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
                // Remember classes units
                myUnits.addAll(sending);
                
                StringBuilder sb = new StringBuilder();
                if (noMoreFiles) {
                    copyMyFiles();
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
                return super.performCustomAction();
            }
        }
        
        /**
         * Copy classes rcode to outputdir
         */
        private void copyMyFiles() {
            String objName;
            String vSrcDir = tempPath.toAbsolutePath().toString();
            String vDestDir = compAttrs.getDestDir() == null ? "" : compAttrs.getDestDir().toString();

            for (Iterator<CompilationUnit> iter = myUnits.iterator(); iter.hasNext();) {
                CompilationUnit cu = iter.next();
                if (!cu.isClass()) continue;
                
                objName = cu.getRcodeFile();
                java.nio.file.Path vSourceFile = Paths.get(vSrcDir, objName );
                java.nio.file.Path vDestFile = Paths.get(vDestDir, objName);
                
                if (vSourceFile.toFile().exists()){
                    try {
                        log("Copie -> " + vSourceFile + " - " + vDestFile, Project.MSG_VERBOSE);
                        File vDir = new File(vDestFile.getParent().toString());
                        if (!vDir.exists()) vDir.mkdirs();
                        Files.copy(vSourceFile, vDestFile, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log("Pas de fichier", e, Project.MSG_DEBUG);
                    }
                }       
            }
            
            
        }

        @Override
        protected String getOptions() {

            tempPath = Paths.get(compAttrs.getDestDir() == null ? 
                    new File("").getAbsolutePath() /* Current directory by default*/ : compAttrs.getDestDir().getAbsolutePath(),
                    TEMPCOMPDIR,Integer.toString(getThreadNumber()));
            // Create tempDir 
            if (!tempPath.toFile().exists()){
                tempPath.toFile().mkdirs();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(super.getOptions());

            sb.append(tempPath).append(';');
            
            return sb.toString();
        }
    }
}
