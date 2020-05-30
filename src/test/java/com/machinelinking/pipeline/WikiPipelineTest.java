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

package com.machinelinking.pipeline;

import com.machinelinking.parser.DocumentSource;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.util.JSONUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Test case for {@link WikiPipeline}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiPipelineTest {

    private static final Logger logger = Logger.getLogger(WikiPipelineTest.class);

    @Test
    public void testEnrich1()
    throws IOException, SAXException, WikiTextParserException, InterruptedException, ExecutionException {
        verifyPipeline(
                new URL("http://en.wikipedia.org/wiki/Albert_Einstein"), true, "Page1.wikitext", "Page1.json"
        );
    }

    @Test
    public void testEnrich2()
    throws IOException, SAXException, WikiTextParserException, InterruptedException, ExecutionException {
        verifyPipeline(
                new URL("http://en.wikipedia.org/wiki/London"), false, "Page2.wikitext", "Page2.json"
        );
    }

    private void verifyPipeline(URL entity, boolean online, String wikiInResource, String jsonOutExpectedResource)
    throws IOException, WikiTextParserException, SAXException, ExecutionException, InterruptedException {
        final List<Flag> flags = new ArrayList<>();
        if(online) flags.add(WikiPipelineFactory.Linkers);
        flags.add(WikiPipelineFactory.Validate);
        flags.add(WikiPipelineFactory.Extractors);
        flags.add(WikiPipelineFactory.Splitters);
        flags.add(WikiPipelineFactory.Structure);

        final InputStream inInputStream = this.getClass().getResourceAsStream(wikiInResource);
        if(inInputStream == null) throw new NullPointerException("Cannot find input resource");
        final InputStream expectedInStream = this.getClass().getResourceAsStream(jsonOutExpectedResource);
        if(expectedInStream == null) throw new NullPointerException("Cannot find expected resource.");

        final WikiPipeline enricher = WikiPipelineFactory
                .getInstance().createFullyConfiguredInstance( flags.toArray( new Flag[flags.size()] ) );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(baos);
        enricher.enrichEntity(
                new DocumentSource(entity, inInputStream),
                serializer
        );
        logger.debug("JSON Output: " + baos);

        final JsonNode expectedJSON = JSONUtils.parseJSON(expectedInStream);
        final JsonNode extractedJSON = JSONUtils.parseJSON(baos.toString());
        removeVariableData(expectedJSON);
        removeVariableData(extractedJSON);
        Assert.assertTrue( expectedJSON.equals(extractedJSON) );
    }

    private void removeVariableData(JsonNode node) {
        final ObjectNode onode = (ObjectNode) node.get("freebase");
        if(onode != null) onode.put("score", 0);
        ((ObjectNode) node).put("abstract", (String) null);
    }

}
