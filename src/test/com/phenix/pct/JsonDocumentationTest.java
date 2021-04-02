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
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

/**
 * Test JsonDocumentation task
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET</a>
 */
public class JsonDocumentationTest extends BuildFileTestNg {

    @Test(groups = {"v11", "win"})
    public void test1() {
        configureProject("JsonDocumentation/test1/build.xml");
        executeTarget("test");

        File f1 = new File("JsonDocumentation/test1/doc/out.json");
        assertTrue(f1.exists());
        Gson gson = new Gson();
        try (Reader r = new FileReader(f1); JsonReader reader = new JsonReader(r)) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            assertEquals(array.size(), 8);
        } catch (IOException caught) {
            fail("Unable to read out.json", caught);
        }

        File f2 = new File("JsonDocumentation/test1/doc2/out.json");
        assertTrue(f2.exists());
        Gson gson2 = new Gson();
        try (Reader r = new FileReader(f2); JsonReader reader = new JsonReader(r)) {
            JsonArray array = gson2.fromJson(reader, JsonArray.class);
            assertEquals(array.size(), 8);
        } catch (IOException caught) {
            fail("Unable to read out.json", caught);
        }
    }

    @Test(groups = {"v11"})
    public void test2() {
        configureProject("JsonDocumentation/test2/build.xml");
        executeTarget("test");

        File f1 = new File("JsonDocumentation/test2/doc/out.json");
        assertTrue(f1.exists());
        Gson gson = new Gson();
        try (Reader r = new FileReader(f1); JsonReader reader = new JsonReader(r)) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);

        } catch (IOException caught) {
            fail("Unable to read out.json", caught);
        }
    }

    @Test(groups = {"v11"})
    public void test3() {
        configureProject("JsonDocumentation/test3/build.xml");
        executeTarget("test");

        File f1 = new File("JsonDocumentation/test3/doc/out.json");
        assertTrue(f1.exists());
        Gson gson = new Gson();
        try (Reader r = new FileReader(f1); JsonReader reader = new JsonReader(r)) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);

        } catch (IOException caught) {
            fail("Unable to read out.json", caught);
        }
    }

}
