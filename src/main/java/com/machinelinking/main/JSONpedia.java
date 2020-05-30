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

package com.machinelinking.main;

import com.machinelinking.dbpedia.InMemoryOntologyManager;
import com.machinelinking.dbpedia.OntologyManager;
import com.machinelinking.dbpedia.OntologyManagerException;
import com.machinelinking.dbpedia.TemplateMappingFactory;
import com.machinelinking.dbpedia.TemplateMappingManager;
import com.machinelinking.dbpedia.TemplateMappingManagerException;
import com.machinelinking.filter.DefaultJSONFilterEngine;
import com.machinelinking.filter.DefaultJSONFilterFactory;
import com.machinelinking.filter.JSONFilter;
import com.machinelinking.filter.JSONFilterEngine;
import com.machinelinking.freebase.FreebaseService;
import com.machinelinking.parser.DocumentSource;
import com.machinelinking.pipeline.WikiPipeline;
import com.machinelinking.pipeline.WikiPipelineFactory;
import com.machinelinking.render.DefaultDocumentContext;
import com.machinelinking.render.DefaultHTMLRenderFactory;
import com.machinelinking.render.DocumentContext;
import com.machinelinking.render.NodeRenderException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.service.BasicServer;
import com.machinelinking.storage.DefaultJSONStorageLoader;
import com.machinelinking.storage.JSONStorage;
import com.machinelinking.storage.JSONStorageLoader;
import com.machinelinking.storage.MultiJSONStorage;
import com.machinelinking.storage.MultiJSONStorageConfiguration;
import com.machinelinking.storage.MultiJSONStorageFactory;
import com.machinelinking.template.RenderScope;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.BufferedWikiPageHandler;
import com.machinelinking.wikimedia.WikiAPIParser;
import com.machinelinking.wikimedia.WikiDumpParser;
import com.machinelinking.wikimedia.WikiPage;
import com.machinelinking.wikimedia.WikimediaUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main library facade.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class JSONpedia {

    private static JSONpedia instance;

    public static JSONpedia instance() {
        if(instance == null) {
            instance = new JSONpedia();
        }
        return instance;
    }

    private OntologyManager ontologyManager;

    private TemplateMappingFactory templateMappingFactory;

    private FreebaseService freebaseService;

    private BasicServer basicServer;

    private MultiJSONStorageFactory multiJSONStorageFactory;

    private JSONpedia() {}

    public OntologyManager getOntologyManager() throws OntologyManagerException {
        if(ontologyManager == null) {
            ontologyManager = new InMemoryOntologyManager();
        }
        return ontologyManager;
    }

    public TemplateMappingManager getTemplateMappingManager(String lang) throws TemplateMappingManagerException {
        if(templateMappingFactory == null) {
            templateMappingFactory = TemplateMappingFactory.getInstance();
        }
        return templateMappingFactory.getTemplateMappingManager(lang);
    }

    public FreebaseService getFreebaseService() {
        if(freebaseService == null) {
            freebaseService = FreebaseService.getInstance();
        }
        return freebaseService;
    }

    public String render(String resource, JsonNode data) throws JSONpediaException {
        final URL resourceURL = JSONUtils.toResourceURL(resource);
        final DocumentContext context = new DefaultDocumentContext(RenderScope.FULL_RENDERING, resourceURL);
        try {
            return DefaultHTMLRenderFactory.getInstance().createRender().renderDocument(context, data);
        } catch (NodeRenderException nre) {
            throw new JSONpediaException("Error while rendering node.", nre);
        }
    }

    public String render(String resource, JsonNode[] data) throws JSONpediaException {
        final ArrayNode arrayNode = JSONUtils.getJsonNodeFactory().arrayNode();
        arrayNode.addAll(Arrays.asList(data));
        return render(resource, arrayNode);
    }

    public void startServer(String host, int port) throws JSONpediaException {
        if(basicServer == null) {
            BasicServer bs = new BasicServer(host, port);
            try {
                bs.setUp();
            } catch (IOException ioe) {
                throw new JSONpediaException("Error while running server.", ioe);
            }
            basicServer = bs;
        } else {
            throw new IllegalStateException("Server already running.");
        }
    }

    public void stopServer() {
        if(basicServer == null) {
            throw new IllegalStateException("No server found.");
        } else {
            try {
                basicServer.tearDown();
            } catch (Exception e) {
                // Pass.
            }
            basicServer = null;
        }
    }

    public JSONStorage getStorage(String configURI) {
        if(multiJSONStorageFactory == null) {
            multiJSONStorageFactory = new MultiJSONStorageFactory();
        }
        final MultiJSONStorageConfiguration configuration =  multiJSONStorageFactory.createConfiguration(configURI);
        final MultiJSONStorage storage = multiJSONStorageFactory.createStorage(configuration);
        final List<JSONStorage> internalStorages = storage.getInternalStorages();
        return internalStorages.size() == 1 ? internalStorages.get(0) : storage;
    }

    public JSONStorageLoader getStorageLoader(String configURI, String flags) {
        final JSONStorage storage = getStorage(configURI);
        return new DefaultJSONStorageLoader(
                WikiPipelineFactory.getInstance(),
                WikiPipelineFactory.getInstance().toFlags(flags),
                storage
        );
    }

    public WikiPage getRawPage(String entity) throws JSONpediaException {
        try {
            return WikiAPIParser.parseAPIResponse(
                    WikimediaUtils.entityToWikiTextURLAPI(JSONUtils.toResourceURL(entity))
            );
        } catch (Exception e) {
            throw new JSONpediaException("Error while retrieving raw page.", e);
        }
    }

    public BufferedWikiPageHandler getRawPagesBuffer(InputStream in) throws JSONpediaException {
        final WikiDumpParser dumpParser = new WikiDumpParser();
        final BufferedWikiPageHandler bufferedHandler = new BufferedWikiPageHandler();
        try {
            dumpParser.parse(
                    bufferedHandler,
                    in
            );
        } catch (Exception e) {
            throw new JSONpediaException("Error while parsing pages stream.", e);
        }
        return bufferedHandler;
    }

    public JsonNode[] applyFilter(String filter, JsonNode node) {
        final JSONFilter filterObj = DefaultJSONFilterEngine.parseFilter(filter);
        final JSONFilterEngine engine = new DefaultJSONFilterEngine();
        return engine.filter(node, filterObj);
    }

    public Output process(String entity) throws JSONpediaException {
        return new Output(new Params(entity));
    }

    private ExecutionResult execute(Params params) throws JSONpediaException {
        try {
            final URL documentURL = JSONUtils.toResourceURL(params.entity);
            final DocumentSource documentSource = params.text ==
                    null ? new DocumentSource(documentURL) : new DocumentSource(documentURL, params.text);
            final WikiPipeline wikiEnricher = WikiPipelineFactory.getInstance()
                    .createFullyConfiguredInstance(params.flags, WikiPipelineFactory.DEFAULT_FLAGS);
            final JSONSerializer jsonSerializer;
            final JSONFilter filter;
            try {
                filter = params.filter == null ?
                        DefaultJSONFilterFactory.EMPTY_FILTER : params.filter;
            } catch (Exception e) {
                throw new IllegalArgumentException("Error while parsing filter.", e);
            }

            final TokenBuffer buffer = JSONUtils.createJSONBuffer();
            try {
                jsonSerializer = new JSONSerializer(buffer);
            } catch (IOException ioe) {
                throw new IllegalStateException("Error while initializing serializer.", ioe);
            }

            wikiEnricher.enrichEntity(documentSource, jsonSerializer);
            final TokenBuffer filtered = JSONUtils.createResultFilteredObject(buffer, filter);
            final JsonNode filteredJSON = JSONUtils.bufferToJSONNode(filtered);
            return new ExecutionResult(documentURL, filteredJSON);
        } catch (Exception e) {
            throw new JSONpediaException("Error while processing entity: " + params.entity, e);
        }
    }

    public class Output {
        private final Params params;

        public Output(Params params) {
            this.params = params;
        }

        public Output text(String text) {
            this.params.text = text;
            return this;
        }

        public Output flags(String flags) {
            this.params.flags = flags;
            return this;
        }

        public Output filter(JSONFilter filter) {
            this.params.filter = filter;
            return this;
        }

        public Output filter(String filter) {
            this.params.filter = DefaultJSONFilterEngine.parseFilter(filter);
            return this;
        }

        public JsonNode json() throws JSONpediaException {
            final ExecutionResult result = execute(params);
            return result.root;
        }

        public String html() throws JSONpediaException {
            try {
                final ExecutionResult result = execute(params);
                final DocumentContext context = new DefaultDocumentContext(
                        RenderScope.FULL_RENDERING,
                        result.documentURL
                );
                return DefaultHTMLRenderFactory.getInstance().createRender().renderDocument(context, result.root);
            } catch (Exception e) {
                throw new JSONpediaException("Error while rendering entity to HTML", e);
            }
        }

        public Map<String,?> map() throws JSONpediaException {
            final ExecutionResult result = execute(params);
            try {
                return JSONUtils.convertNodeToMap(result.root);
            } catch (IOException ioe) {
                throw new JSONpediaException("Error while converting root node to POJO.", ioe);
            }
        }
    }

    private class Params {
        final String entity;
        String text;
        String flags;
        JSONFilter filter;
        private Params(String entity) {
            this.entity = entity;
        }
    }

    private class ExecutionResult {
        private final URL documentURL;
        private final JsonNode root;
        ExecutionResult(URL documentURL, JsonNode root) {
            this.documentURL = documentURL;
            this.root = root;
        }
    }

}
