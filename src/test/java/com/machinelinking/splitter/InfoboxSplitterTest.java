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

import com.machinelinking.parser.MultiWikiTextParserHandler;
import com.machinelinking.parser.WikiTextParserHandler;
import com.machinelinking.serializer.JSONSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Test case for {@link InfoboxSplitter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InfoboxSplitterTest {

    @Test
    public void testSplitter() throws IOException {
        final InfoboxSplitter splitter = new InfoboxSplitter();
        final WikiTextParserHandlerSplitter splitterHandler = new WikiTextParserHandlerSplitter();
        splitter.initHandlerSplitter(splitterHandler);
        final MultiWikiTextParserHandler multiHandler = new MultiWikiTextParserHandler();
        multiHandler.add(splitterHandler.getProxy());
        multiHandler.add(splitter);

        multiHandler.beginDocument(new URL("http://test/doc"));
        multiHandler.beginTemplate(new WikiTextParserHandler.TemplateName("t1"));
        multiHandler.parameter("t1-p1");
        multiHandler.beginTemplate(new WikiTextParserHandler.TemplateName("tt1"));
        splitter.split(); // < Split here
        multiHandler.parameter("tt1-p1");
        multiHandler.text("text 1");
        multiHandler.text("text 2");
        multiHandler.endTemplate(new WikiTextParserHandler.TemplateName("tt1"));
        multiHandler.parameter("t1-p2");
        multiHandler.text("text 3");
        multiHandler.endTemplate(new WikiTextParserHandler.TemplateName("t1"));
        multiHandler.endDocument();

        Assert.assertEquals(splitterHandler.getActiveRedirections().size(), 0);
        final Map<String,WikiTextParserHandlerSplitter.Redirect> redirections =
                splitterHandler.getcompletedRedirections();
        Assert.assertEquals(redirections.size(), 1);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(baos);
        serializer.openObject();
        splitter.serialize(serializer);
        serializer.closeObject();
        serializer.flush();
        Assert.assertEquals(
            baos.toString(),
            "{\"infobox-splitter\":[{\"@type\":\"template\",\"name\":\"tt1\"," +
            "\"content\":{\"tt1-p1\":[\"text 1\",\"text 2\"]}}]}"
        );
    }

}
