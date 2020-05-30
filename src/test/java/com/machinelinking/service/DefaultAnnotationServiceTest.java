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

package com.machinelinking.service;

import com.machinelinking.filter.DefaultJSONFilterEngineTest;
import com.machinelinking.pipeline.WikiPipelineFactory;
import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultAnnotationServiceTest extends ServiceTestBase {

    private static final String[] EXPECTED_ARRAY_NODES = {
        "sections", "links", "references"
    };

    private static final String[] EXPECTED_OBJECT_NODES = {
        "templates", "categories"
    };

    private static final String TARGET_RESOURCE;

    static {
        try {
            TARGET_RESOURCE = "resource/json/" + URLEncoder.encode(
                    "http://en.wikipedia.org/wiki/Albert_Einstein", "UTF8"
            );
        } catch (UnsupportedEncodingException urise) {
            throw new IllegalStateException();
        }
    }

    @Test
    public void testFlags() throws IOException, URISyntaxException, ConnectionException {
        final JsonNode node = performQuery(buildPath(DefaultAnnotationService.class, "flags").build());
        Assert.assertEquals(
                WikiPipelineFactory.getInstance().getDefinedFlags().length,
                node.get("definedFlags").size()
        );
    }

    @Test
    public void testAnnotate() throws IOException, URISyntaxException, ConnectionException {
        checkJSONResponse( performQuery(buildPath(DefaultAnnotationService.class, TARGET_RESOURCE).build()) );
    }

    @Test
    public void testAnnotateOnline() throws IOException, URISyntaxException, ConnectionException {
        final JsonNode node = performQuery(
                buildPath(DefaultAnnotationService.class, TARGET_RESOURCE)
                    .queryParam("procs", WikiPipelineFactory.Linkers).build()
        );
        checkJSONResponse(node);
        Assert.assertNotNull(node.get("freebase"));
    }

    @Test
    public void testAnnotateWithFilters() throws URISyntaxException, IOException, ConnectionException {
        final JsonNode node = performQuery(
                buildPath(DefaultAnnotationService.class, TARGET_RESOURCE)
                        .queryParam("procs", WikiPipelineFactory.Structure)
                        .queryParam("filter", DefaultJSONFilterEngineTest.STRING_FILTER_EXP).build()
        );
        Assert.assertEquals(
                node.toString(),
                JSONUtils.parseJSON(
                        "{\"filter\":\"object_filter(@type=template,name=Death date and age,)>null\"," +
                                "\"result\":[{\"@type\":\"template\",\"name\":\"Death date and age\"," +
                                "\"content\":{\"df\":[\"yes\"],\"@an0\":[\"1955\"],\"@an1\":[\"4\"]," +
                                "   \"@an2\":[\"18\"],\"@an3\":[\"1879\"],\"@an4\":[\"3\"],\"@an5\":[\"14\"]}}]}"
                ).toString()
        );
    }

    private void checkJSONResponse(JsonNode node) {
        for (String expectedNode : EXPECTED_ARRAY_NODES) {
            final JsonNode content = node.get(expectedNode);
            Assert.assertNotNull(content, "Cannot find object node: " + expectedNode);
            Assert.assertTrue(content.isArray(), "Invalid content for " + expectedNode);
        }
        for (String expectedNode : EXPECTED_OBJECT_NODES) {
            final JsonNode content = node.get(expectedNode);
            Assert.assertNotNull(content, "Cannot find object node: " + expectedNode);
            Assert.assertTrue(content.isObject(), "Invalid content for " + expectedNode);
        }
    }

}
