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

package com.machinelinking.dbpedia;

import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Test case for {@link TemplateMappingHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateMappingHandlerTest {

    // http://mappings.dbpedia.org/index.php?title=Mapping:Infobox_scientist&action=edit

    @Test
    public void testMappingReading() throws IOException, WikiTextParserException {
        final TemplateMapping[] out = new TemplateMapping[1];
        final TemplateMappingHandler handler = new TemplateMappingHandler("Mapping1") {
            @Override
            public void handle(TemplateMapping mapping) {
                out[0] = mapping;
            }
        };
        final WikiTextParser parser = new WikiTextParser(handler);
        parser.parse(
                new URL("http://test/url"),
                new BufferedReader(new InputStreamReader(
                        this.getClass().getResourceAsStream(
                            "/com/machinelinking/dbpedia/Mapping1.wikitext"
                        )
                ))
        );
        final TemplateMapping templateMapping = out[0];
        Assert.assertEquals(templateMapping.getMappingName(), "Mapping1");
        Assert.assertEquals(templateMapping.getMappingClass(), "Congressman");
        Assert.assertEquals(templateMapping.getMappingSize(), 396);
    }

}
