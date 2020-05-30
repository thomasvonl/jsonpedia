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
import com.machinelinking.wikimedia.WikiAPIParser;
import com.machinelinking.wikimedia.WikiPage;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Factory for {@link com.machinelinking.dbpedia.TemplateMappingManager}s
 * and {@link com.machinelinking.dbpedia.TemplateMapping}s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
//TODO: factory is not using template cache currently. Need to be refactored.
public class TemplateMappingFactory {

    private static final Logger logger = Logger.getLogger(TemplateMappingFactory.class);

    private static final String MAPPING_PREFIX = "Mapping:";
    private static final OntologyManager ontologyManager;
    private static TemplateMappingFactory instance;

    static {
        try {
            ontologyManager = OntologyManagerFactory.getInstance().getOntologyManager();
        } catch (OntologyManagerException ome) {
            throw new RuntimeException("Error while initializing ontology manager.", ome);
        }
    }

    public static TemplateMappingFactory getInstance() {
        if(instance == null) {
            instance = new TemplateMappingFactory();
        }
        return instance;
    }

    private TemplateMappingFactory() {}

    public TemplateMappingManager getTemplateMappingManager(String lang) throws TemplateMappingManagerException {
        return new InMemoryTemplateMappingManager(lang);
    }

    public TemplateMapping readMappingForTemplate(String mappingName)
    throws IOException, WikiTextParserException, SAXException {
        final URL templateMappingURL = DBpediaUtils.templateToWikiMappingAPIURL(MAPPING_PREFIX + mappingName);
        WikiPage wikiTextMapping;
        try {
            wikiTextMapping = WikiAPIParser.parseAPIResponse(templateMappingURL);
        } catch (Exception e) {
            wikiTextMapping = null;
            logger.warn(String.format("An error occurred while retrieving mapping [%s]", mappingName), e);
        }

        if (wikiTextMapping != null) {
            final TemplateMapping[] out = new TemplateMapping[1];
            final TemplateMappingHandler handler = new TemplateMappingHandler(mappingName) {
                @Override
                public void handle(TemplateMapping mapping) {
                    out[0] = mapping;
                }
            };
            final WikiTextParser parser = new WikiTextParser(handler);
            parser.parse(templateMappingURL, new ByteArrayInputStream(wikiTextMapping.getContent().getBytes()));
            return out[0];
        } else {
            return null;
        }
    }

    public TemplateMapping createMapping(String mappingName, String mappingClass) {
        final TemplateMapping tm = new TemplateMapping(mappingName, mappingClass);
        tm.setOntologyManager(ontologyManager);
        return tm;
    }

}
