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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Class for testing PCTDynamicRun task
 * 
 * @author <a href="mailto:justus_phenix@users.sourceforge.net">Gilles QUERRET </a>
 */
public class PCTDynamicRunTest extends BuildFileTestNg {

    @Test(groups = {"v11"})
    public void test1() {
        configureProject("PCTDynamicRun/test1/build.xml");
        executeTarget("init");

        List<String> rexp = new ArrayList<>();
        rexp.add("This is dir1");
        rexp.add("val1");
        rexp.add("val2");
        rexp.add("num-dbs: 2");
        rexp.add("num-aliases: 6");
        rexp.add("Output val1val2");
        expectLogRegexp("test1", rexp, true);

        expectLog("test2", "This is dir2");
    }

    @Test(groups = {"v11"})
    public void test2() {
        configureProject("PCTDynamicRun/test2/build.xml");
        expectLog("test", "Output val1 val2");
    }

    @Test(groups = {"v11"})
    public void test3() {
        configureProject("PCTDynamicRun/test3/build.xml");
        executeTarget("init");

        List<String> rexp = new ArrayList<>();
        rexp.add("This is dir1");
        rexp.add("val1");
        rexp.add("val2");
        rexp.add("num-dbs: 2");
        rexp.add("num-aliases: 6");
        rexp.add("val3");
        rexp.add("Output val1val2");
        expectLogRegexp("test1", rexp, true);
    }
    
    @Test(groups = {"v11"})
    public void test4() {
        configureProject("PCTDynamicRun/test4/build.xml");
        executeTarget("init");
        expectLog("test", "Output val1 val2");
    }

}
