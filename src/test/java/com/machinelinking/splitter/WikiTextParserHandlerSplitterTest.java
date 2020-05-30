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
 * Test case for {@link WikiTextParserHandlerSplitter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserHandlerSplitterTest {

    private WikiTextParserHandlerSplitter splitter;

    @BeforeMethod
    public void setUp() {
        splitter = new WikiTextParserHandlerSplitter();
    }

    @Test
    public void testSplitting() throws MalformedURLException {
        final String REDIRECTION_NAME = "test-redirection";
        final WikiTextParserHandler proxy = splitter.getProxy();
        proxy.beginDocument( new URL("http://path/to/test") );
        proxy.beginTemplate(new WikiTextParserHandler.TemplateName("t1"));
        proxy.parameter("p1");
        proxy.text("text0");
        proxy.parameter("p2");
        proxy.beginTemplate(new WikiTextParserHandler.TemplateName("tt1"));

        // Redirect from here.
        final WikiTextParserHandlerEventBuffer buffer = splitter.createRedirection(REDIRECTION_NAME);
        Assert.assertEquals(splitter.getActiveRedirections().size(), 1);
        Assert.assertEquals(splitter.getActiveRedirections().get(0).id, REDIRECTION_NAME);

        proxy.parameter("pp1");
        proxy.text("text1");
        proxy.text("text2");
        proxy.text("text3");
        proxy.endTemplate(new WikiTextParserHandler.TemplateName("tt1"));
        proxy.beginTemplate(new WikiTextParserHandler.TemplateName("tt2"));
        proxy.parameter("pp11");
        proxy.text("text pp11");
        proxy.endTemplate(new WikiTextParserHandler.TemplateName("tt2"));
        proxy.parameter("p3");
        proxy.text("text4");
        proxy.endTemplate(new WikiTextParserHandler.TemplateName("t1"));
        proxy.endDocument();

        Assert.assertEquals(splitter.getActiveRedirections().size(), 0);
        Assert.assertEquals(splitter.getcompletedRedirections().size(), 1);
        Assert.assertNotNull(splitter.getcompletedRedirections().get(REDIRECTION_NAME));

        final WikiTextHRDumperHandler out = new WikiTextHRDumperHandler();
        buffer.flush(out);
        Assert.assertEquals(
            out.getContent(),

            "Begin Template: tt1\n" +
            "k: pp1\n" +
            "Text: 'text1'\n" +
            "Text: 'text2'\n" +
            "Text: 'text3'\n" +
            "End Template: tt1\n"
        );
    }

}
