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
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class for testing ClassDocumentation task
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET</a>
 */
public class ClassDocumentationTest extends BuildFileTestNg {
    private final XPath xpath = XPathFactory.newInstance().newXPath();

    @Test(groups= {"win", "v11"}, enabled = false)
    public void test1() throws XPathExpressionException {
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

        InputSource inputSource = new InputSource("ClassDocumentation/test1/doc/eu.rssw.pct.X.xml");
        assertEquals(xpath.evaluate("//unit/temp-table[@name='tt3']/@like", inputSource), "tt1");
        assertEquals(xpath.evaluate("//unit/temp-table[@name='tt3']/field[@name='fld5']/@dataType", inputSource), "UNKNOWN DATATYPE");
        // assertEquals(xpath.evaluate("//unit/temp-table[@name='tt3']/field[@name='fld5']/@like", inputSource), "tt1.fld1");
    }

    @Test(groups= {"v11"}, enabled = false)
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

    @Test(groups= {"win", "v11"}, enabled = false)
    public void test3() {
        configureProject("ClassDocumentation/test3/build.xml");
        executeTarget("test");
    }

    @Test(groups= {"win", "v11"}, enabled = false)
    public void test4() {
        configureProject("ClassDocumentation/test4/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test4/doc/TestClass.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test4/html/TestClass.html");
        assertTrue(f2.exists());
    }

    @Test(groups= {"win", "v11"}, enabled = false)
    public void test5() {
        configureProject("ClassDocumentation/test5/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test5/doc/OpenEdge.Core.InjectABL.InjectionRequest.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test5/html/OpenEdge.Core.InjectABL.InjectionRequest.html");
        assertTrue(f2.exists());
    }

    @Test(groups= {"win", "v11"}, enabled = false)
    public void test6() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        configureProject("ClassDocumentation/test6/build.xml");
        executeTarget("test");

        File f1 = new File("ClassDocumentation/test6/doc/rssw.pct.TestClass1.xml");
        assertTrue(f1.exists());
        File f2 = new File("ClassDocumentation/test6/doc/rssw.pct.TestClass2.xml");
        assertTrue(f2.exists());
        File f3 = new File("ClassDocumentation/test6/doc/rssw.pct.TestClass3.xml");
        assertTrue(f3.exists());

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xPath.compile("//unit/method[@methodName='method2']");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new FileInputStream(f3));
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        assertEquals(nodeList.getLength(), 1);
    }

}
