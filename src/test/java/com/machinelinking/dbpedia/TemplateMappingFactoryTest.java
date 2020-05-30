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

import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.util.JSONUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Test case for {@link com.machinelinking.dbpedia.TemplateMappingFactory} lookup.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateMappingFactoryTest {

    private static final Logger logger = Logger.getLogger(TemplateMappingFactoryTest.class);

    @Test
    public void testToJSON() throws IOException, WikiTextParserException, SAXException {
        final TemplateMapping mapping = TemplateMappingFactory.getInstance().readMappingForTemplate("Chembox");
        final String json = JSONUtils.serializeToJSON(mapping);
        logger.debug(json);
        Assert.assertEquals(
                JSONUtils.parseJSON(json),
                JSONUtils.parseJSON( this.getClass().getResourceAsStream("Mapping1.json") )
        );
    }

    @Test
    public void testReadMappingForTemplate() throws IOException, WikiTextParserException, SAXException {
        final TemplateMapping mapping = TemplateMappingFactory.getInstance().readMappingForTemplate("Infobox scientist");
        logger.debug(JSONUtils.serializeToJSON(mapping));
        Assert.assertEquals(mapping.getMappingSize(), 23);
    }

}
