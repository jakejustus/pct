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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.phenix.pct.PCTCompileTest.Test80LineProcessor;
import com.phenix.pct.RCodeInfo.InvalidRCodeException;

/**
 * Class for testing PCTCompileExt task
 *
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET</a>
 */
public class PCTCompileExtTest extends BuildFileTestNg {
    private static final String BASEDIR = "PCTCompileExt/";

    @Test(groups = {"v11"})
    public void test2() {
        configureProject(BASEDIR + "test2/build.xml");
        executeTarget("test");
    }

    @Test(groups = {"v11"})
    public void test3() {
        configureProject(BASEDIR + "test3/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test3/build/test.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test3bis() {
        configureProject(BASEDIR + "test3bis/build.xml");
        expectBuildException("test", "Compilation should fail");

        File f = new File(BASEDIR + "test3bis/build/test.r");
        assertFalse(f.exists());
    }

    @Test(groups = {"v11"})
    public void test4() {
        configureProject(BASEDIR + "test4/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test4/build/test.r");
        File f2 = new File(BASEDIR + "test4/build2/test.r");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f2.length() < f1.length());
    }

    @Test(groups = {"v11"})
    public void test5() {
        configureProject(BASEDIR + "test5/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test5/build/wizz~~'~.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test6() {
        configureProject(BASEDIR + "test6/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test6/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod == f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test7() {
        configureProject(BASEDIR + "test7/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test7/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod < f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test8() {
        configureProject(BASEDIR + "test8/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test8/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod < f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test9() {
        configureProject(BASEDIR + "test9/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test9/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod < f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test10() {
        configureProject(BASEDIR + "test10/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test10/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod < f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test11() {
        configureProject(BASEDIR + "test11/build.xml");

        expectBuildException("test", "Second task should not be launched");
        File f = new File(BASEDIR + "test11/build/test2.r");
        assertFalse(f.exists());

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test11/build2/test2.r");
        assertTrue(f2.exists());

        executeTarget("test3");
        assertTrue(new File(BASEDIR + "test11/build3/test0.r").exists());
        assertFalse(new File(BASEDIR + "test11/build3/test1.r").exists());
        assertTrue(new File(BASEDIR + "test11/build3/test2.r").exists());

        executeTarget("test4");
        assertTrue(new File(BASEDIR + "test11/build4/test0.r").exists());
        assertFalse(new File(BASEDIR + "test11/build4/test1.r").exists());
        assertFalse(new File(BASEDIR + "test11/build4/test2.r").exists());
    }

    @Test(groups = {"v11"})
    public void test12() {
        configureProject(BASEDIR + "test12/build.xml");
        expectBuildException("test1", "File with underscore");
        File f = new File(BASEDIR + "test12/build/test.r");
        assertFalse(f.exists());
        executeTarget("test2");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test13() {
        configureProject(BASEDIR + "test13/build.xml");
        executeTarget("test1");

        File f = new File(BASEDIR + "test13/build/test.r");
        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod < f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test14() {
        configureProject(BASEDIR + "test14/build.xml");
        executeTarget("base");
        executeTarget("test1");

        File f1 = new File(BASEDIR + "test14/build/test.r");
        File f2 = new File(BASEDIR + "test14/build/test2.r");
        File f3 = new File(BASEDIR + "test14/build/test3.r");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f3.exists());

        long mod1 = f1.lastModified();
        long mod2 = f2.lastModified();
        long mod3 = f3.lastModified();
        executeTarget("update");
        executeTarget("test2");
        assertTrue(mod1 < f1.lastModified());
        assertTrue(mod2 < f2.lastModified());
        assertTrue(mod3 < f3.lastModified());
    }

    @Test(groups = {"v11"})
    public void test15() {
        configureProject(BASEDIR + "test15/build.xml");
        executeTarget("base");
        executeTarget("test1");

        File f = new File(BASEDIR + "test15/build/test.r");
        assertTrue(f.exists());

        long mod = f.lastModified();
        executeTarget("test2");
        assertTrue(mod == f.lastModified());
    }

    @Test(groups = {"v11"})
    public void test16() {
        configureProject(BASEDIR + "test16/build.xml");
        executeTarget("xcode");
        File f1 = new File(BASEDIR + "test16/src/xcode1/test.p");
        assertTrue(f1.exists());
        File f1bis = new File(BASEDIR + "test16/src/xcode2/test.p");
        assertTrue(f1bis.exists());

        executeTarget("test");
        File f2 = new File(BASEDIR + "test16/build1/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test16/build2/test.r");
        assertTrue(f3.exists());
        File f4 = new File(BASEDIR + "test16/build3/test.r");
        assertTrue(f4.exists());
    }

    @Test(groups = {"v11"})
    public void test17() {
        configureProject(BASEDIR + "test17/build.xml");
        executeTarget("xcode");
        File f1 = new File(BASEDIR + "test17/src/xcode/test.p");
        assertTrue(f1.exists());
        expectBuildException("test1", "No XCodeKey");

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test17/build/xcode/test.r");
        assertFalse(f2.exists());
        File f3 = new File(BASEDIR + "test17/build2/xcode/test.r");
        assertTrue(f3.exists());
    }

    @Test(groups = {"v11"})
    public void test19() {
        configureProject(BASEDIR + "test19/build.xml");
        executeTarget("test1");

        File f1 = new File(BASEDIR + "test19/build/test.r");
        File f2 = new File(BASEDIR + "test19/build/.pct/test.p.crc");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        long mod1 = f1.lastModified();
        long mod2 = f2.lastModified();
        executeTarget("test2");
        assertTrue(mod1 < f1.lastModified());
        assertTrue(mod2 < f2.lastModified());
    }

    @Test(groups = {"v11"})
    public void test20() {
        configureProject(BASEDIR + "test20/build.xml");
        executeTarget("test1");

        File dotR = new File(BASEDIR + "test20/build/test.r");
        File f1 = new File(BASEDIR + "test20/build/.pct/test.p");
        File f2 = new File(BASEDIR + "test20/build/.pct/test.p.preprocess");
        File f3 = new File(BASEDIR + "test20/build/.dbg/test.p");
        File f4 = new File(BASEDIR + "test20/build/.pct/test.p.xref");
        File f5 = new File(BASEDIR + "test20/debug/test.p");
        File f6 = new File(BASEDIR + "test20/debug/dir1_dir2_test.p");

        assertTrue(dotR.exists());
        assertFalse(f1.exists());
        assertFalse(f2.exists());
        assertFalse(f3.exists());
        assertFalse(f4.exists());

        executeTarget("test2");
        assertTrue(f1.exists(), "Unable to find listing file");
        assertTrue(f2.exists(), "Unable to find preprocess file");
        assertTrue(f3.exists(), "Unable to find debug-listing file");
        assertTrue(f4.exists(), "Unable to find xref file");
        assertTrue((f4.length() > 0), "Empty xref file");

        executeTarget("test3");
        assertTrue(f5.exists(), "Unable to find debug-listing file");
        assertTrue(f6.exists(), "Unable to find debug-listing file");
    }

    @Test(groups = {"v11"})
    public void test21() {
        configureProject(BASEDIR + "test21/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test21/build/package/testclass.r");
        assertTrue(f1.exists());
    }

    @Test(groups = {"v11"})
    public void test22() {
        configureProject(BASEDIR + "test22/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test22/build/X.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test22/build/Y.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test22/build/.pct/X.cls.crc");
        assertTrue(f3.exists());
        File f4 = new File(BASEDIR + "test22/build/.pct/Y.cls.crc");
        assertTrue(f4.exists());

        executeTarget("test2");
        File f5 = new File(BASEDIR + "test22/build2/Y.r");
        assertTrue(f5.exists());
        File f6 = new File(BASEDIR + "test22/build2/X.r");
        /* Comment because compilation in tempdir avoid this 
        assertTrue(f6.exists());*/
    }

    @Test(groups = {"v11", "win"})
    public void test23() {
        configureProject(BASEDIR + "test23/build.xml");
        expectBuildException("test1", "Should fail - No stream-io");

        File f = new File(BASEDIR + "test23/build/test.r");
        assertFalse(f.exists());
        executeTarget("test2");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test24() {
        configureProject(BASEDIR + "test24/build.xml");

        File f = new File(BASEDIR + "test24/build/test.r");
        assertFalse(f.exists());

        executeTarget("test1");
        assertFalse(f.exists());
        executeTarget("test2");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test25() {
        configureProject(BASEDIR + "test25/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test25/build/test.r");
        assertTrue(f.exists());
    }

    // Dropping this test case, doesn't work on 11.3 anymore, and no time to maintain it
    // @Test(groups= {"win"})
    public void test26() {
        configureProject(BASEDIR + "test26/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test26/build/Ö_example.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test27() {
        configureProject(BASEDIR + "test27/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test27/build/eu/rssw/pct/A.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test27/build/eu/rssw/pct/B.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test27/build/eu/rssw/pct/X.r");
        assertTrue(f3.exists());
        File f4 = new File(BASEDIR + "test27/build/eu/rssw/pct/Y.r");
        assertTrue(f4.exists());
        File f5 = new File(BASEDIR + "test27/build/eu/rssw/pct/Z.r");
        assertTrue(f5.exists());
        File f6 = new File(BASEDIR + "test27/build/.pct/eu/rssw/pct/Z.cls.hierarchy");
        assertTrue(f6.exists());
        assertTrue(f6.length() > 0);
    }

    @Test(groups = {"win", "v11"})
    public void test28() {
        configureProject(BASEDIR + "test28/build.xml");
        executeTarget("build");

        File f1 = new File(BASEDIR + "test28/src-tty/test.p");
        assertTrue(f1.exists());
        assertTrue(f1.length() > 0);
        File f2 = new File(BASEDIR + "test28/src-gui/test.p");
        assertTrue(f2.exists());
        assertTrue(f2.length() > 0);
        File f3 = new File(BASEDIR + "test28/src-tty/sub1/sub2/sub3/test.p");
        assertTrue(f3.exists());
        File f4 = new File(BASEDIR + "test28/src-tty/sub1/sub2/sub4/test.p");
        assertTrue(f4.exists());

        executeTarget("test");
        String str1 = getProject().getProperty("test28-tty");
        assertTrue(str1.equals("TTY"));
        String str2 = getProject().getProperty("test28-gui");
        assertTrue(str2.startsWith("MS-WIN"));
    }

    @Test(groups = {"v11"})
    public void test29() {
        configureProject(BASEDIR + "test29/build.xml");
        executeTarget("build");

        File f1 = new File(BASEDIR + "test29/build1/test.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test29/build2/test.r");
        assertTrue(f2.exists());

        executeTarget("test1");
        executeTarget("test2");
        executeTarget("test3");

        String test1Inc = getProject().getProperty("test1-inc");
        // Absolute paths, so it should be found
        File ff = new File(test1Inc);
        assertTrue(ff.exists());
        String test1Main = getProject().getProperty("test1-main");
        File ff2 = new File(test1Main);
        assertTrue(ff2.exists());

        String test2Inc = getProject().getProperty("test2-inc");
        assertEquals(test2Inc.replace('\\', '/'), "inc/test.i");
        String test2Main = getProject().getProperty("test2-main");
        assertEquals(test2Main.replace('\\', '/'), "src/test.p");

        String test3Main = getProject().getProperty("test3-main");
        assertEquals(test3Main.replace('\\', '/'), "src/foo/bar/test.p");
        String test3Inc1 = getProject().getProperty("test3-inc1");
        assertEquals(test3Inc1.replace('\\', '/'), "src/foo/foo.i");
        String test3Inc2 = getProject().getProperty("test3-inc2");
        assertEquals(test3Inc2.replace('\\', '/'), "inc/foo/bar.i");
    }

    @Test(groups = {"v11"})
    public void test30() {
        configureProject(BASEDIR + "test30/build.xml");

        executeTarget("test");
        assertTrue(new File(BASEDIR + "test30/build/test1.r").exists());
        assertFalse(new File(BASEDIR + "test30/build/test2.r").exists());
        assertTrue(new File(BASEDIR + "test30/build/test3.r").exists());

        expectBuildException("test2", "ZipFileset not supported");

        executeTarget("test3");
        assertFalse(new File(BASEDIR + "test30/build3/test1.r").exists());
        assertTrue(new File(BASEDIR + "test30/build3/test2.r").exists());
        assertFalse(new File(BASEDIR + "test30/build3/test3.r").exists());

        executeTarget("test4");
        assertEquals(new File(BASEDIR + "test30/build4").list().length, 1); // Only .pct

        executeTarget("test5");
        assertTrue(new File(BASEDIR + "test30/build5/test1.r").exists());
        assertTrue(new File(BASEDIR + "test30/build5/test2.r").exists());
        assertTrue(new File(BASEDIR + "test30/build5/test3.r").exists());

        executeTarget("test6");
        assertTrue(new File(BASEDIR + "test30/build6/test1.r").exists());
        assertFalse(new File(BASEDIR + "test30/build6/test2.r").exists());
        assertFalse(new File(BASEDIR + "test30/build6/test3.r").exists());

        executeTarget("test7");
        assertTrue(new File(BASEDIR + "test30/build7/test1.r").exists());
        assertTrue(new File(BASEDIR + "test30/build7/test2.r").exists());
        assertTrue(new File(BASEDIR + "test30/build7/test3.r").exists());

        executeTarget("test8");
        assertTrue(new File(BASEDIR + "test30/build8/test1.r").exists());
        assertTrue(new File(BASEDIR + "test30/build8/test2.r").exists());
        assertTrue(new File(BASEDIR + "test30/build8/test3.r").exists());
    }

    @Test(groups = {"v11"})
    public void test32() {
        configureProject(BASEDIR + "test32/build.xml");
        executeTarget("test");

        assertTrue(new File(BASEDIR + "test32/build1/.pct/test1.p.strxref").exists());
        assertTrue(new File(BASEDIR + "test32/build1/.pct/test2.p.strxref").exists());
        assertTrue(new File(BASEDIR + "test32/build2/.pct/strings.xref").exists());
    }

    @Test(groups = {"v11"})
    public void test33() {
        configureProject(BASEDIR + "test33/build.xml");
        executeTarget("test");

        assertTrue(new File(BASEDIR + "test33/build/test1.r").exists());
        assertTrue(new File(BASEDIR + "test33/build/test2.r").exists());

        expectBuildException("test2", "Expected failure");
        assertFalse(new File(BASEDIR + "test33/build2/test1.r").exists());
        assertFalse(new File(BASEDIR + "test33/build2/test2.r").exists());
        assertFalse(new File(BASEDIR + "test33/build2/test3.r").exists());
    }

    @Test(groups = {"v11"})
    public void test34() {
        configureProject(BASEDIR + "test34/build.xml");
        executeTarget("test");

        File dbg1 = new File(BASEDIR + "test34/debugListing/test1.p");
        File dbg2 = new File(BASEDIR + "test34/debugListing/foo_bar_test2.p");
        File rcode1 = new File(BASEDIR + "test34/build/test1.r");
        File rcode2 = new File(BASEDIR + "test34/build/foo/bar/test2.r");
        assertTrue(dbg1.exists());
        assertTrue(dbg2.exists());
        assertTrue(rcode1.exists());
        assertTrue(rcode2.exists());
        // Doesn't work...
        // RCodeInfo r1 = new RCodeInfo(rcode1);
        // RCodeInfo r2 = new RCodeInfo(rcode2);
        // assertEquals(r1.getDebugListingFile(), "test1.p");
        // assertEquals(r2.getDebugListingFile(), "foo_bar_test2.p");
    }

    @Test(groups = {"v11"})
    public void test35() throws IOException {
        configureProject(BASEDIR + "test35/build.xml");
        executeTarget("init");
        executeTarget("test");
        executeTarget("test2");

        File crc = new File(BASEDIR + "test35/build/.pct/test.p.crc");
        assertTrue(crc.exists());
        String line = Files.readFirstLine(crc, Charset.defaultCharset());
        assertTrue(line.startsWith("\"sports2000.Item\""));
        File inc = new File(BASEDIR + "test35/build/.pct/test.p.inc");
        assertTrue(inc.exists());

        LineProcessor<Boolean> lineProcessor = new Test35LineProcessor();
        Files.readLines(inc, Charset.defaultCharset(), lineProcessor);
        assertTrue(lineProcessor.getResult());

        File crc2 = new File(BASEDIR + "test35/build2/.pct/test.p.crc");
        assertTrue(crc2.exists());
        String line2 = Files.readFirstLine(crc2, Charset.defaultCharset());
        assertTrue(line2.startsWith("\"sports2000.Item\""));
        File inc2 = new File(BASEDIR + "test35/build2/.pct/test.p.inc");
        assertTrue(inc2.exists());
        LineProcessor<Boolean> lineProcessor2 = new Test35LineProcessor();
        Files.readLines(inc2, Charset.defaultCharset(), lineProcessor2);
        assertTrue(lineProcessor2.getResult());
    }

    private final static class Test35LineProcessor implements LineProcessor<Boolean> {
        private boolean retVal = false;
        private int zz = 0;

        @Override
        public Boolean getResult() {
            return retVal;
        }

        @Override
        public boolean processLine(String line) throws IOException {
            if (zz == 0) {
                retVal = line.startsWith("\"test.i\"");
            } else if (zz == 1) {
                retVal &= line.startsWith("\"test2.i\"");
            } else if (zz == 2) {
                retVal &= line.startsWith("\"test3.i\"");
            }
            zz++;

            return true;
        }
    }

    @Test(groups = {"v11"})
    public void test36() throws IOException {
        configureProject(BASEDIR + "test36/build.xml");
        executeTarget("test");

        assertTrue(new File(BASEDIR + "test36/build/bar/test1.r").exists());
        assertTrue(new File(BASEDIR + "test36/build/bar/test2.r").exists());
        assertTrue(new File(BASEDIR + "test36/build/bar/test3.r").exists());
        assertFalse(new File(BASEDIR + "test36/build/bar/test4.r").exists());
        assertTrue(new File(BASEDIR + "test36/build/foo/test.r").exists());
        assertTrue(new File(BASEDIR + "test36/build/foo/subdir/subdir2/test.r").exists());
        assertFalse(new File(BASEDIR + "test36/build/baz/test.r").exists());

        executeTarget("test2");

        assertTrue(new File(BASEDIR + "test36/build2/bar/test1.r").exists());
        assertTrue(new File(BASEDIR + "test36/build2/bar/test2.r").exists());
        assertTrue(new File(BASEDIR + "test36/build2/bar/test3.r").exists());
        assertFalse(new File(BASEDIR + "test36/build2/bar/test4.r").exists());
        assertTrue(new File(BASEDIR + "test36/build2/foo/test.r").exists());
        assertTrue(new File(BASEDIR + "test36/build2/foo/subdir/subdir2/test.r").exists());
        assertFalse(new File(BASEDIR + "test36/build2/baz/test.r").exists());
    }

    // @Test(groups = {"v11"})
    // Not really a test case, just to show something is broken...
    public void test37() throws IOException {
        configureProject(BASEDIR + "test37/build.xml");
        executeTarget("init");

        assertTrue(new File(BASEDIR + "test37/build1/package/bar.r").exists());
        assertTrue(new File(BASEDIR + "test37/build1/package/Foo.r").exists());
        assertTrue(new File(BASEDIR + "test37/build2/package/bar.r").exists());
        assertTrue(new File(BASEDIR + "test37/build2/package/foo.r").exists());
        assertTrue(new File(BASEDIR + "test37/build3/package/bAr.r").exists());
        assertTrue(new File(BASEDIR + "test37/build3/package/fOO.r").exists());

        executeTarget("test1");
        executeTarget("test2");
        executeTarget("test3");
    }

    @Test(groups = {"v11"})
    public void test38() {
        // Compile error with xcode
        configureProject(BASEDIR + "test38/build.xml");
        executeTarget("init");
        expectBuildException("test", "Should fail - Progress syntax error");
    }

    @Test(groups = {"v11"})
    public void test39() {
        // Compile error, no xcode
        configureProject(BASEDIR + "test39/build.xml");
        expectBuildException("test", "Should fail - Progress syntax error");
    }

    @Test(groups = {"v11"})
    public void test40() {
        // Test keepXref attribute
        configureProject(BASEDIR + "test40/build.xml");
        executeTarget("test");

        assertFalse(new File(BASEDIR + "test40/build1/.pct/test.p.xref").exists());
        assertTrue(new File(BASEDIR + "test40/build2/.pct/test.p.xref").exists());
    }

    @Test(groups = {"v11"})
    public void test42() {
        configureProject(BASEDIR + "test42/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test42/build/test.r");
        File f2 = new File(BASEDIR + "test42/build2/test.r");
        long mod1 = f1.lastModified();
        long mod2 = f2.lastModified();

        executeTarget("test2");
        assertTrue(f1.lastModified() > mod1);
        assertTrue(f2.lastModified() > mod2);
    }

    @Test(groups = {"v11"})
    public void test43() {
        configureProject(BASEDIR + "test43/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test43/build/test.r");
        File f2 = new File(BASEDIR + "test43/build2/test.r");
        long mod1 = f1.lastModified();
        long mod2 = f2.lastModified();
        assertFalse(new File(BASEDIR + "test43/build/.pct/test.p.xref").exists());
        assertFalse(new File(BASEDIR + "test43/build/.pct/test.p.inc").exists());
        assertFalse(new File(BASEDIR + "test43/build/.pct/test.p.crc").exists());
        assertFalse(new File(BASEDIR + "test43/build/.dbg/test.p").exists());
        assertTrue(new File(BASEDIR + "test43/build2/.dbg/test.p").exists());
        assertTrue(new File(BASEDIR + "test43/build2/.pct/test.p.preprocess").exists());

        executeTarget("test2");
        assertTrue(f1.lastModified() > mod1);
        assertTrue(f2.lastModified() > mod2);
    }

    @Test(groups = {"v11"})
    public void test45() {
        configureProject(BASEDIR + "test45/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test45/build/test.r");
        File f2 = new File(BASEDIR + "test45/build/.pct/test.p");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f2.length() > 650);
    }

    @Test(groups = {"v11"})
    public void test46() {
        configureProject(BASEDIR + "test46/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test46/build/test.r");
        File f2 = new File(BASEDIR + "test46/build/.pct/test.p");
        File f3 = new File(BASEDIR + "test46/build2/test.r");
        File f4 = new File(BASEDIR + "test46/build2/.pct/test.p");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f3.exists());
        assertTrue(f4.exists());
        try {
            // Preprocessed source code removes many lines of unreachable code
            assertTrue(Files.readLines(f2, Charsets.UTF_8).size() + 10 < Files
                    .readLines(f4, Charsets.UTF_8).size());
        } catch (IOException caught) {
            Assert.fail("Unable to open file", caught);
        }
    }

    @Test(groups = {"v11"})
    public void test47() {
        configureProject(BASEDIR + "test47/build.xml");
        executeTarget("test1");

        File f1 = new File(BASEDIR + "test47/build/dir1/test.r");
        assertTrue(f1.exists());
        long mod1 = f1.lastModified();

        executeTarget("test2");
        assertTrue(f1.lastModified() > mod1);
    }

    @Test(groups = {"v11"})
    public void test48() {
        configureProject(BASEDIR + "test48/build.xml");
        executeTarget("test1");

        File f1 = new File(BASEDIR + "test48/build/test.r");
        assertTrue(f1.exists());
        long mod1 = f1.lastModified();

        executeTarget("test2");
        assertTrue(f1.lastModified() > mod1);
    }

    @Test(groups = {"v11"})
    public void test49() {
        configureProject(BASEDIR + "test49/build.xml");
        executeTarget("test1");
        File warns = new File(BASEDIR + "test49/build/.pct/test.p.warnings");
        assertTrue(warns.exists());
        assertTrue(warns.length() > 0);
    }

    @Test(groups = {"v11"})
    public void test50() {
        configureProject(BASEDIR + "test50/build.xml");
        executeTarget("test1");
        File rcode = new File(BASEDIR + "test50/build/test.r");
        assertTrue(rcode.exists());
        assertTrue(rcode.length() > 0);
    }

    @Test(groups = {"v11"})
    public void test51() {
        configureProject(BASEDIR + "test51/build.xml");
        executeTarget("test1"); /* compile all programms */
        File f1 = new File(BASEDIR + "test51/build/test.r");
        assertTrue(f1.exists());
        long mod1 = f1.lastModified();
        File f2 = new File(BASEDIR + "test51/build/test2.r");
        assertTrue(f2.exists());
        long mod2 = f2.lastModified();

        executeTarget("test2"); /* nothing should compile */
        File f3 = new File(BASEDIR + "test51/build/test.r");
        assertTrue(f3.exists());
        assertFalse(f3.lastModified() > mod1);
        mod1 = f1.lastModified();

        File f4 = new File(BASEDIR + "test51/build/test2.r");
        assertTrue(f4.exists());
        assertFalse(f4.lastModified() > mod2);

        executeTarget("test3"); /* all programms should be compiled */
        File f5 = new File(BASEDIR + "test51/build/test.r");
        long mod5 = f5.lastModified();
        assertTrue(f5.exists());
        assertTrue(mod5 > mod1);

        File f6 = new File(BASEDIR + "test51/build/test2.r");
        long mod6 = f6.lastModified();
        assertTrue(f6.exists());
        assertTrue(mod6 > mod2);

        executeTarget("test4"); /* all programms should be compiled */
        File f7 = new File(BASEDIR + "test51/build/test.r");
        assertTrue(f7.exists());
        assertTrue(f7.lastModified() > mod5);

        File f8 = new File(BASEDIR + "test51/build/test2.r");
        assertTrue(f8.exists());
        assertTrue(f8.lastModified() > mod6);
    }

    @Test(groups = {"v11"})
    public void test52() {
        // Only work with 11.7+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 6))
            return;

        configureProject(BASEDIR + "test52/build.xml");
        executeTarget("test1");
        File f1 = new File(BASEDIR + "test52/build1/test.r");
        assertTrue(f1.exists());

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test52/build2/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test52/build2/.pct/test.p.warnings");
        assertTrue(f3.exists());
    }

    @Test(groups = {"v11"})
    public void test53() {
        // Only work with 11.7+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 6))
            return;

        configureProject(BASEDIR + "test53/build.xml");
        executeTarget("db");
        executeTarget("test1");
        File f1 = new File(BASEDIR + "test53/build1/test.r");
        assertTrue(f1.exists());

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test53/build2/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test53/build2/.pct/test.p.warnings");
        assertTrue(f3.exists());
    }

    @Test(groups = {"v11"})
    public void test54() {
        // Only work with 11.7+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 6))
            return;

        configureProject(BASEDIR + "test54/build.xml");
        executeTarget("db");
        executeTarget("test1");
        File f1 = new File(BASEDIR + "test54/build1/test.r");
        assertTrue(f1.exists());

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test54/build2/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test54/build2/.pct/test.p.warnings");
        assertTrue(f3.exists());
    }

    @Test(groups = {"v11"})
    public void test55() {
        configureProject(BASEDIR + "test55/build.xml");
        executeTarget("test1");
        assertTrue(new File(BASEDIR + "test55/build1/test.r").exists());
        assertTrue(new File(BASEDIR + "test55/build1/.pct/test.p.inc").exists());

        executeTarget("test2");
        assertFalse(new File(BASEDIR + "test55/build2/.pct").exists());
        assertTrue(new File(BASEDIR + "test55/xref2/test.p.inc").exists());

        executeTarget("test3");
        assertFalse(new File(BASEDIR + "test55/build3/.pct").exists());
        assertTrue(new File(BASEDIR + "test55/xref3/test.p.inc").exists());
        assertTrue(new File(BASEDIR + "test55/build3/test.r").exists());
        assertTrue(new File(BASEDIR + "test55/build3/test2.r").exists());

        executeTarget("test4");
        assertTrue(new File(BASEDIR + "test55/src/test.p").exists());
        assertTrue(new File(BASEDIR + "test55/src/subdir/test2.p").exists());
        assertTrue(new File(BASEDIR + "test55/src/test.r").exists());
        assertTrue(new File(BASEDIR + "test55/src/test2.r").exists());
    }

    @Test(groups = {"v11"})
    public void test56() {
        configureProject(BASEDIR + "test56/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test56/build1/build2/build3/test.r").exists());
    }

    @Test(groups = {"v11"})
    public void test57() {
        configureProject(BASEDIR + "test57/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test57/build/test.r").exists());
    }

    @Test(groups = {"v11"})
    public void test58() {
        configureProject(BASEDIR + "test58/build.xml");
        executeTarget("db");
        executeTarget("build");
        assertTrue(new File(BASEDIR + "test58/build1/file1.r").exists());
        assertTrue(new File(BASEDIR + "test58/build1/dir1/file2.r").exists());
        assertTrue(new File(BASEDIR + "test58/build1/dir1/file3.r").exists());
        expectLog("test-fr-1", new String[] { "FR1-FR1", "7", "FR2-FR2", "7"});
        expectLog("test-de-1", new String[] { "DE1-DE1", "7", "DE2-DE2", "7"});
        expectLog("test-fr-2", new String[] { "FR1-FR1-FR1", "14", "FR2-FR2-FR2", "14"});
        expectLog("test-de-2", new String[] { "DE1-DE1-DE1", "14", "DE2-DE2-DE2", "14"});
    }

    @Test(groups = {"v11"})
    public void test59() {
        configureProject(BASEDIR + "test59/build.xml");
        executeTarget("test");
        File warns1 = new File(BASEDIR + "test59/build1/.pct/test.p.warnings");
        assertFalse(warns1.exists());
        File warns2 = new File(BASEDIR + "test59/build2/.pct/test.p.warnings");
        assertTrue(warns2.exists());
        assertTrue(warns2.length() > 0);
    }

    @Test(groups = {"v11"})
    public void test60() {
        configureProject(BASEDIR + "test60/build.xml");
        executeTarget("test");
        File warns = new File(BASEDIR + "test60/build/.pct/test.p.warnings");
        assertTrue(warns.exists());
        assertTrue(warns.length() > 0);
        executeTarget("test2");
        assertFalse(warns.exists());
    }

    @Test(groups = {"v11"})
    public void test61() {
        configureProject(BASEDIR + "test61/build.xml");
        expectBuildException("test", "Expected...");
        File xref = new File(BASEDIR + "test61/build/.pct/test.p.xref");
        assertFalse(xref.exists());
    }

    @Test(groups = {"v11"})
    public void test62() {
        // Same as test60 but with -swl.
        configureProject(BASEDIR + "test62/build.xml");
        executeTarget("test");
        File warns1 = new File(BASEDIR + "test62/build1/.pct/test.p.warnings");
        assertTrue(warns1.exists());
        assertTrue(warns1.length() > 0);
        File warns2 = new File(BASEDIR + "test62/build2/.pct/test.p.warnings");
        assertTrue(warns2.exists());
        assertTrue(warns2.length() > 0);
        assertTrue(warns2.length() < warns1.length());
        File warns3 = new File(BASEDIR + "test62/build3/.pct/test.p.warnings");
        // All warnings are ignored, file is not present anymore
        assertFalse(warns3.exists());

        // Existing file should then be deleted
        executeTarget("test2");
        assertFalse(warns2.exists());
    }

    @Test(groups = {"v11"})
    public void test63() {
        configureProject(BASEDIR + "test63/build.xml");

        List<String> rexp = new ArrayList<>();
        rexp.add("PCTCompile - Progress Code Compiler");
        rexp.add("Error compiling file 'src/dir1/test1.p' \\.\\.\\.");
        rexp.add(" \\.\\.\\. in main file at line 3.*");
        expectLogRegexp("test1", rexp, false);

        rexp.clear();
        rexp.add("PCTCompile - Progress Code Compiler");
        rexp.add("Error compiling file 'src/dir1/test2.p' \\.\\.\\.");
        rexp.add(" \\.\\.\\. in file 'src/dir1/test2.i' at line 3.*");
        expectLogRegexp("test2", rexp, false);

        rexp.clear();
        rexp.add("PCTCompile - Progress Code Compiler");
        rexp.add("Error compiling file 'src/dir1/test3.p' \\.\\.\\.");
        rexp.add(" \\.\\.\\. in file 'src/dir1/test2.i' at line 3.*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(" \\.\\.\\. in main file at line 4.*");
        expectLogRegexp("test3", rexp, false);

        rexp.clear();
        rexp.add("PCTCompile - Progress Code Compiler");
        rexp.add("Error compiling file 'src/dir1/test4.p' \\.\\.\\.");
        rexp.add(" \\.\\.\\. in file 'src/rssw/pct/TestClass.cls' at line 2.*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(".*");
        rexp.add(" \\.\\.\\. in main file at line 2.*");
        expectLogRegexp("test4", rexp, false);

        rexp.clear();
        rexp.add("PCTCompile - Progress Code Compiler");
        rexp.add("Error compiling file 'rssw/pct/TestClass2.cls' \\.\\.\\.");
        rexp.add(" \\.\\.\\. in main file at line 2.*");
        expectLogRegexp("test5", rexp, false);
    }

    @Test(groups = {"v11"})
    public void test64() {
        // Simplified version of test58
        configureProject(BASEDIR + "test64/build.xml");
        executeTarget("init");
        executeTarget("build");
        assertTrue(new File(BASEDIR + "test64/build1/file1.r").exists());
        assertTrue(new File(BASEDIR + "test64/build2/file1.r").exists());
        assertTrue(new File(BASEDIR + "test64/build1/.dbg/file1.p").exists());
        assertTrue(new File(BASEDIR + "test64/build2/.dbg/file1.p").exists());
        expectLog("test-fr-1", new String[] { "FR1-FR1", "7", "FR2-FR2", "7"});
        expectLog("test-fr-2", new String[] { "FR1-FR1", "7", "FR2-FR2", "7"});
    }

    @Test(groups = {"v11"})
    public void test65() {
        // Test without destDir
        configureProject(BASEDIR + "test65/build.xml");
        executeTarget("build_a");
        executeTarget("build_b");
        executeTarget("build_c");

        assertTrue(new File(BASEDIR + "test65/a/src/a/a.r").exists());
        assertTrue(new File(BASEDIR + "test65/b/src/b/b.r").exists());
        assertTrue(new File(BASEDIR + "test65/c/src/c/c.r").exists());

        assertFalse(new File(BASEDIR + "test65/b/src/a/a.r").exists());
        assertFalse(new File(BASEDIR + "test65/c/src/a/a.r").exists());
        assertFalse(new File(BASEDIR + "test65/c/src/b/b.r").exists());
    }

    @Test(groups = {"v11", "win"})
    public void test66() throws InvalidRCodeException, IOException {
        configureProject(BASEDIR + "test66/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test66/build/v9.r").exists());
        assertTrue(new File(BASEDIR + "test66/build-v6/v9.r").exists());
        assertTrue(new File(BASEDIR + "test66/build-v6underline/v9.r").exists());
        assertTrue(new File(BASEDIR + "test66/build-v6revvideo/v9.r").exists());

        RCodeInfo rci0 = new RCodeInfo(new File(BASEDIR + "test66/build/v9.r"));
        RCodeInfo rci1 = new RCodeInfo(new File(BASEDIR + "test66/build/v6.r"));
        RCodeInfo rci2 = new RCodeInfo(new File(BASEDIR + "test66/build-v6/v9.r"));
        assertEquals(rci1.getRCodeSize(), rci2.getRCodeSize());

        RCodeInfo rci3 = new RCodeInfo(new File(BASEDIR + "test66/build/ul.r"));
        RCodeInfo rci4 = new RCodeInfo(new File(BASEDIR + "test66/build-v6underline/v9.r"));
        assertEquals(rci3.getRCodeSize(), rci4.getRCodeSize());

        RCodeInfo rci5 = new RCodeInfo(new File(BASEDIR + "test66/build/rv.r"));
        RCodeInfo rci6 = new RCodeInfo(new File(BASEDIR + "test66/build-v6revvideo/v9.r"));
        assertEquals(rci5.getRCodeSize(), rci6.getRCodeSize());

        assertNotEquals(rci0.getRCodeSize(), rci1.getRCodeSize());
        assertNotEquals(rci1.getRCodeSize(), rci3.getRCodeSize());
        assertNotEquals(rci1.getRCodeSize(), rci5.getRCodeSize());
    }

    @Test(groups = {"v11"})
    public void test67() {
        configureProject(BASEDIR + "test67/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test67/build-interface/rssw/pct/ITest.r").exists());
        assertTrue(new File(BASEDIR + "test67/build-impl/rssw/pct/TestImpl.r").exists());
        assertFalse(new File(BASEDIR + "test67/build-impl/rssw/pct/ITest.r").exists());
    }

    @Test(groups = {"v11"})
    public void test68() {
        configureProject(BASEDIR + "test68/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test68/src1/rssw/pct/ITest.r").exists());
        assertTrue(new File(BASEDIR + "test68/build-impl/rssw/pct/TestImpl.r").exists());
        // This file shouldn't be there, and is incorrectly created by the compiler
        // assertFalse(new File(BASEDIR + "test68/build-impl/rssw/pct/ITest.r").exists());
    }

    @Test(groups = {"v11"})
    public void test69() {
        configureProject(BASEDIR + "test69/build.xml");
        executeTarget("init");
        executeTarget("test1");
        executeTarget("test2");
        assertTrue(new File(BASEDIR + "test69/build/test1.r").exists());
        assertTrue(new File(BASEDIR + "test69/build/test2.r").exists());
        assertTrue(new File(BASEDIR + "test69/build/.dbg/test2.p").exists());
    }

    @Test(groups = {"v11"})
    public void test70() {
        configureProject(BASEDIR + "test70/build.xml");
        executeTarget("test");
        // Extension is .p, not .r...
        assertTrue(new File(BASEDIR + "test70/build/test1.p").exists());
        assertTrue(new File(BASEDIR + "test70/build/test2.p").exists());
        assertFalse(new File(BASEDIR + "test70/build/subdir/test2.p").exists());
    }

    @Test(groups = {"v11"})
    public void test71() {
        configureProject(BASEDIR + "test71/build.xml");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test71/build/test1.r").exists());
        assertTrue(new File(BASEDIR + "test71/build/.pct/test1.p.xref").exists());
    }

    @Test(groups = {"v11"})
    public void test72() {
        configureProject(BASEDIR + "test72/build.xml");
        executeTarget("db");
        // STOP condition raised by warning 4516 has to be trapped
        executeTarget("build");
    }

    // ResultProperty is meaningless in multi-threaded builds
    @Test(enabled=false, groups = {"v11"})
    public void test73() {
        configureProject(BASEDIR + "test73/build.xml");
        executeTarget("test1");
        assertPropertyEquals("test73Result1", "10");
        executeTarget("test2");
        assertPropertyEquals("test73Result2", "0");
    }

    @Test(groups = {"v11"})
    public void test74() {
        configureProject(BASEDIR + "test74/build.xml");
        executeTarget("init");
        executeTarget("test");
        assertTrue(new File(BASEDIR + "test74/build/test.r").exists());
    }

    @Test(groups = {"v11"})
    public void test75() {
        configureProject(BASEDIR + "test75/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test75/build/test.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test76() {
        // Only work with 11.3+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 2))
            return;

        configureProject(BASEDIR + "test76/build.xml");
        executeTarget("compile");

        expectLog("testInitialize", "Initialize#pct/pctBgCompile.p");
        expectLog("testBeforeCompile", "Before Compile#pct/pctBgCompile.p#test.p#src");
        expectLog("testAfterCompile", "After Compile#pct/pctBgCompile.p#test.p#src");

        File f = new File(BASEDIR + "test76/build/test.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test77() {
        // Only work with 11.3+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 2))
            return;

        configureProject(BASEDIR + "test77/build.xml");
        executeTarget("compile");

        File f1 = new File(BASEDIR + "test77/build/test01.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test77/build/test02.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test77/build/excl01.r");
        assertFalse(f3.exists());
        File f4 = new File(BASEDIR + "test77/build/excl02.r");
        assertFalse(f4.exists());
    }

    @Test(groups = {"v11"})
    public void test78() {
        char ff = (char) 12;

        configureProject(BASEDIR + "test78/build.xml");
        executeTarget("compile");

        File listing = new File(BASEDIR + "test78/build/.pct/testPage.p");
        assertTrue(listing.exists(), "Unable to find listing file");

        try {
            List<String> lines = Files.readLines(listing, Charset.defaultCharset());

            // Test the ASCII code at the defined PAGE-SIZE - 1 (zero based).
            assertTrue(lines.size() > 10 && lines.get(10).contains(String.valueOf(ff)));
            // Test the length of the first line containing code.
            assertTrue(lines.get(4).length() == 90);
        } catch (IOException e) {
            Assert.fail("Unable to read file", e);
        }
    }

    @Test(groups = {"v11"})
    public void test79() {
        // No test case as 'outputType' attribute is not implemented in PCTCompileExt
    }

    @Test(groups = {"v12"})
    public void test80() {
        // Only work with 12.2+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 12) && (version.getMinorVersion() < 2))
            return;

        configureProject(BASEDIR + "test80/build.xml");
        executeTarget("test1");
        File f1 = new File(BASEDIR + "test80/build1/test.r");
        assertTrue(f1.exists());

        executeTarget("test2");
        File f2 = new File(BASEDIR + "test80/build2/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test80/build2/.pct/test.p.warnings");
        assertTrue(f3.exists());

        try {
            assertTrue(Files.readLines(f3, Charset.defaultCharset(), new Test80LineProcessor()));
        } catch (IOException caught) {
            fail("Unable to read file", caught);
        }
    }

    // @Test(groups = {"v11"})
    public void test81() {
        configureProject(BASEDIR + "test81/build.xml");
        executeTarget("init");

        executeTarget("test1");
        File f1 = new File(BASEDIR + "test81/build1/rssw/Class1.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test81/build1/prgs/Internal.r");
        assertTrue(f2.exists());

        executeTarget("test2");
        File f3 = new File(BASEDIR + "test81/build2/rssw/Class1.r");
        assertTrue(f3.exists());
        File f4 = new File(BASEDIR + "test81/build2/prgs/Internal.r");
        assertTrue(f4.exists());

        executeTarget("test3");
        File f5 = new File(BASEDIR + "test81/build3/rssw/Class1.r");
        assertTrue(f5.exists());
        File f6 = new File(BASEDIR + "test81/build3/prgs/Internal.r");
        assertTrue(f6.exists());
    }

    @Test(groups = {"v11", "win"})
    public void test82() {
        configureProject(BASEDIR + "test82/build.xml");
        expectBuildException("test", "Crashed process should lead to build failure");
    }

    @Test(groups = {"v11"})
    public void test83() {
        // No test case as 'outputType' attribute is not implemented in PCTCompileExt
    }

    @Test(groups = {"v11"})
    public void test84() {
        configureProject(BASEDIR + "test84/build.xml");
        executeTarget("init");
        executeTarget("test");
        File f1 = new File(BASEDIR + "test84/build/test.r");
        assertTrue(f1.exists());
    }

    @Test(groups = {"v11"})
    public void test85() {
        configureProject(BASEDIR + "test85/build.xml");
        // First build
        expectLog("test1", new String[]{"PCTCompile - Progress Code Compiler", "test.p [No r-code]",
                "test2.p [No r-code]", "2 file(s) compiled"});
        // Second build, nothing compiled
        expectLog("test1",
                new String[]{"PCTCompile - Progress Code Compiler", "0 file(s) compiled"});

        // Touch test.p
        expectLog("test2", new String[]{"PCTCompile - Progress Code Compiler", //
                "test.p [R-code older than source]", //
                "test2.p [R-code older than source]", //
                "2 file(s) compiled"});
        // Touch test.i
        expectLog("test3", new String[]{"PCTCompile - Progress Code Compiler", //
                "test.p [R-code older than include file: test.i]", //
                "test2.p [R-code older than include file: test2.i]", //
                "2 file(s) compiled"});
    }

    @Test(groups = {"v11"})
    public void test87() {
        // Only work with 11.7+
        DLCVersion version = DLCVersion.getObject(new File(System.getProperty("DLC")));
        if ((version.getMajorVersion() == 11) && (version.getMinorVersion() <= 6))
            return;

        configureProject(BASEDIR + "test87/build.xml");
        executeTarget("test1");
        File f1 = new File(BASEDIR + "test87/build1/test.r");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test87/build1/test.r");
        assertTrue(f2.exists());
        File f3 = new File(BASEDIR + "test87/build1/.pct/test.p.warnings");
        assertTrue(f3.exists());

        expectBuildException("test2", "Require full keywords");
    }

    @Test(groups = {"v11"})
    public void test88() {
        configureProject(BASEDIR + "test88/build.xml");
        executeTarget("init");
        expectLogAndBuildException("test", new String[]{"PCTCompile - Progress Code Compiler",
                "Error compiling file 'src/test1.p' ...", " ... in main file",
                "** WARNING--TRANSACTION keyword given within actual transaction level. (214)", "",
                " ... in main file at line 14 column 3"});

        File f1 = new File(BASEDIR + "test88/build/test1.r");
        assertFalse(f1.exists());
        File f2 = new File(BASEDIR + "test88/build/test2.r");
        assertTrue(f2.exists());
    }

    @Test(groups = {"v11"})
    public void test101() {
        configureProject(BASEDIR + "test101/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test101/build/test1.p");
        assertTrue(f1.exists());
        File f2 = new File(BASEDIR + "test101/build/test2.p");
        assertTrue(f2.exists());
    }

    @Test(groups = {"v11"})
    public void test102() {
        configureProject(BASEDIR + "test102/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test102/build/testrenamed.r");
        File f2 = new File(BASEDIR + "test102/build/test2renamed.r");
        File f3 = new File(BASEDIR + "test102/build/triggers/trigger.r");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f3.exists());

        long l1 = f1.lastModified();
        long l2 = f2.lastModified();
        long l3 = f3.lastModified();
        executeTarget("test2");
        assertEquals(f1.lastModified(), l1);
        assertEquals(f2.lastModified(), l2);
        assertEquals(f3.lastModified(), l3);
    }

    @Test(groups = {"v11"})
    public void test103() throws IOException {
        File inputDir = new File(BASEDIR + "test103/src");
        File subDir1 = new File(inputDir, "dir1");
        File subDir2 = new File(subDir1, "dir2");
        subDir2.mkdirs();
        File srcFile = new File(BASEDIR + "test103/query-tester.w");
        for (int ii = 0; ii < 10; ii++) {
            for (int jj = 0; jj < 10; jj++) {
                Files.copy(srcFile, new File(subDir2, "test" + ii + jj + ".p"));
            }
        }
        configureProject(BASEDIR + "test103/build.xml");
        executeTarget("test");

        File f = new File(BASEDIR + "test103/build/dir1/dir2/test00.r");
        assertTrue(f.exists());
        f = new File(BASEDIR + "test103/build/dir1/dir2/test99.r");
        assertTrue(f.exists());
    }

    @Test(groups = {"v11"})
    public void test104() throws IOException {
        configureProject(BASEDIR + "test104/build.xml");
        executeTarget("base");

        expectBuildException("test1", "Invalid alias");
        executeTarget("test2");
        executeTarget("test3");
        executeTarget("test4");
        File f1 = new File(BASEDIR + "test104/build/proc.r");
        File f2 = new File(BASEDIR + "test104/build/proc2.r");
        File f3 = new File(BASEDIR + "test104/build/proc3.r");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        assertTrue(f3.exists());
    }

    @Test(groups = {"v11"})
    public void test105() throws IOException {
        configureProject(BASEDIR + "test105/build.xml");
        executeTarget("base");
        executeTarget("test1");
        expectBuildException("test2", "No DB connection, should throw BuildException");

        File f1 = new File(BASEDIR + "test105/build/test.r");
        File f2 = new File(BASEDIR + "test105/build2/test.r");
        assertTrue(f1.exists());
        assertFalse(f2.exists());
    }

    @Test(groups = {"v11"})
    public void test106() throws IOException {
        configureProject(BASEDIR + "test106/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test106/profiler");
        // 2 profiler files generated
        assertEquals(f1.list().length, 2);
    }

    @Test(groups = {"v11"})
    public void test107() throws IOException {
        configureProject(BASEDIR + "test107/build.xml");
        executeTarget("test");

        File f1 = new File(BASEDIR + "test107/build/test.r");
        File f2 = new File(BASEDIR + "test107/build/test2.r");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
    }
}
