/*
 * Copyright 2012-2015 Michele Mostarda (me@michelemostarda.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.machinelinking.pagestruct;

import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.serializer.Serializer;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test case for {@link WikiTextSerializerHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextSerializerHandlerTest extends WikiTextSerializerHandlerTestBase {

    public WikiTextSerializerHandlerTest() {
        super(TestWikiTextSerializerHandler.class);
    }

    @Test
    public void testPageSerialization1() throws IOException, WikiTextParserException {
        verifySerialization("Page1");
    }

    @Test
    public void testPageSerialization2() throws IOException, WikiTextParserException {
        verifySerialization("Page2");
    }

    @Test
    public void testTemplateSerialization1() throws IOException, WikiTextParserException {
        verifySerialization("Template1");
    }

    @Test
    public void testBrokenTemplateSerialization1() throws IOException, WikiTextParserException {
        verifySerialization("BrokenTemplate1");
    }

    @Test
    public void testTableSerialization1() throws IOException, WikiTextParserException {
        verifySerialization("Table1");
    }

    @Test
    public void testTableSerialization2() throws IOException, WikiTextParserException {
        verifySerialization("Table2");
    }

    @Test
    public void testTableSerialization3() throws IOException, WikiTextParserException {
        verifySerialization("Table3");
    }

    public static class TestWikiTextSerializerHandler extends WikiTextSerializerHandler {

        public TestWikiTextSerializerHandler(Serializer serializer) {
            super(serializer);
        }

    }

}
