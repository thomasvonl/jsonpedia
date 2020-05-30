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

import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.serializer.Serializer;
import com.machinelinking.util.JSONUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Base test case. for {@link WikiTextSerializerHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextSerializerHandlerTestBase <T extends WikiTextSerializerHandler> {

    private static final Logger logger = Logger.getLogger(WikiTextSerializerHandlerTestBase.class);

    private final Class<T> handlerClass;
    private T handler;

    public WikiTextSerializerHandlerTestBase(Class<T> handlerClass) {
        this.handlerClass = handlerClass;
    }

    protected T getHandler() {
        return handler;
    }

    protected void verifySerialization(String wikiPage, String expectedJSON)
    throws IOException, WikiTextParserException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Serializer serializer = new JSONSerializer(baos);
        try {
            this.handler = this.handlerClass.getConstructor(Serializer.class).newInstance(serializer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final WikiTextParser parser = new WikiTextParser(handler);

        final String actual;
        try {
            parser.parse(
                    new URL("http://test/" + wikiPage),
                    new BufferedReader(
                            new InputStreamReader(
                                    this.getClass().getResourceAsStream(String.format("%s.wikitext", wikiPage))
                            )
                    )
            );
        } finally {
            actual = baos.toString();
            logger.debug("Serialization: " + actual);
        }

        final JsonNode actualJSONNode   = JSONUtils.parseJSON(actual);
        final JsonNode expectedJSONNode = JSONUtils.parseJSON(
            this.getClass().getResourceAsStream(String.format("%s.json", expectedJSON))
        );

        Assert.assertEquals(actualJSONNode.toString(), expectedJSONNode.toString(), "Unexpected serialization.");
    }

    protected void verifySerialization(String wikiPage) throws IOException, WikiTextParserException {
        verifySerialization(wikiPage, wikiPage);
    }

}
