/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package com.phenix.pct;

import static org.testng.Assert.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

/**
 * Class for testing ClassDocumentation task
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET</a>
 */
public class ClassDocumentationTest extends BuildFileTestNg {

    @Test(groups= {"win", "v11"})
    public void test1() {
        configureProject("ClassDocumentation/test1/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.X.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.Y.xml");
        assertTrue(f2.exists());
        File f3 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.Z.xml");
        assertTrue(f3.exists());
        File f4 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.A.xml");
        assertTrue(f4.exists());
        File f5 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.B.xml");
        assertTrue(f5.exists());
        File f6 = new File("ClassDocumentation/test1/doc2/dir1/test.p.xml");
        assertTrue(f6.exists());
        File f7 = new File("ClassDocumentation/test1/doc/eu.rssw.pct.TestClass.xml");
        assertTrue(f7.exists());
    }

    @Test(groups= {"v11"})
    public void test2() {
        configureProject("ClassDocumentation/test2/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test2/doc/eu.rssw.pct.X.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test2/doc/eu.rssw.pct.Y.xml");
        assertTrue(f2.exists());
        File f3 = new File("ClassDocumentation/test2/doc/eu.rssw.pct.Z.xml");
        assertTrue(f3.exists());
        File f4 = new File("ClassDocumentation/test2/doc/eu.rssw.pct.A.xml");
        assertTrue(f4.exists());
        File f5 = new File("ClassDocumentation/test2/doc/eu.rssw.pct.B.xml");
        assertTrue(f5.exists());
        File f6 = new File("ClassDocumentation/test2/doc/dir1/test.p.xml");
        assertTrue(f6.exists());
    }

    @Test(groups= {"win", "v11"})
    public void test3() {
        configureProject("ClassDocumentation/test3/build.xml");
        executeTarget("test");
    }

    @Test(groups= {"win", "v11"})
    public void test4() {
        configureProject("ClassDocumentation/test4/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test4/doc/TestClass.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test4/html/TestClass.html");
        assertTrue(f2.exists());
    }
}
