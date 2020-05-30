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

package com.machinelinking.splitter;

import com.machinelinking.pagestruct.WikiTextHRDumperHandler;
import com.machinelinking.parser.WikiTextParserHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test case for {@link WikiTextParserHandlerEventBuffer}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserHandlerEventBufferTest {

    private WikiTextParserHandlerEventBuffer buffer;

    @BeforeMethod
    public void setUp() {
        buffer = new WikiTextParserHandlerEventBuffer();
    }

    @Test
    public void testBuffer() throws MalformedURLException {
        final WikiTextParserHandler handler = buffer.getProxy();
        handler.beginDocument( new URL("http://some.doc"));
        handler.beginTemplate(new WikiTextParserHandler.TemplateName("T1"));
        handler.parameter("p1");
        handler.text("t1");
        handler.text("t2");
        handler.text("t4");
        handler.endTemplate(new WikiTextParserHandler.TemplateName("T1"));
        handler.endDocument();

        Assert.assertEquals(buffer.size(), 8);
        final WikiTextHRDumperHandler serializer = new WikiTextHRDumperHandler();
        buffer.flush(serializer);
        Assert.assertEquals(
                serializer.getContent(),

                "Begin Document\n" +
                "Begin Template: T1\n" +
                "k: p1\n" +
                "Text: 't1'\n" +
                "Text: 't2'\n" +
                "Text: 't4'\n" +
                "End Template: T1\n" +
                "End Document\n"
        );
    }

}
