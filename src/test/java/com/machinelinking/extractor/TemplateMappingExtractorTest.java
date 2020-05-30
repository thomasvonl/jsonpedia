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

import com.machinelinking.dbpedia.TemplateMapping;
import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateMappingExtractorTest {

    @Test
    public void testFetchMapping() throws IOException, WikiTextParserException, ExecutionException, InterruptedException {
        final TemplateMappingExtractor extractor = new TemplateMappingExtractor();
        final WikiTextParser parser = new WikiTextParser(extractor);
        parser.parse(new URL("http://test/page1"), this.getClass().getResourceAsStream("Page1.wikitext"));

        final Map<String, TemplateMapping> mappings = extractor.getCollectedMappings();
        Assert.assertNotNull(mappings);
        final TemplateMapping infoboxScientist = mappings.get("Infobox scientist");
        Assert.assertNotNull(infoboxScientist);
        Assert.assertTrue(infoboxScientist.getMappingSize() > 20);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(baos);
        extractor.flushContent(serializer);
        serializer.flush();
        final JsonNode node = JSONUtils.parseJSON(baos.toString());
        Assert.assertTrue( node.get("mapping-collection").get(0).get("mapping").size() > 20 );
    }


}
