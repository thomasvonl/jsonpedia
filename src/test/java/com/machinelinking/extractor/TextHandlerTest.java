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

package com.machinelinking.extractor;

import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.parser.WikiTextParserFilteredHandler;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;

/**
 * Test case for {@link TextHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TextHandlerTest {

    @Test
    public void testAbstractTextExtraction() throws IOException, WikiTextParserException {
        final TextHandler textHandler = new TextHandler();
        final WikiTextParser parser = new WikiTextParser(
                new WikiTextParserFilteredHandler(textHandler, AbstractFilteredHandlerCriteria.INSTANCE)
        );
        parser.parse(
                new URL("http://test/page1"),
                this.getClass().getResourceAsStream("Page1.wikitext")
        );

        Assert.assertEquals(
                textHandler.flushContent().trim(),
                IOUtils.toString(this.getClass().getResourceAsStream("Page1-texthandler.txt")),
                "Invalid text extraction"
        );
    }

}
