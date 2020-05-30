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

import com.machinelinking.extractor.Extractor;
import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.pagestruct.WikiTextSerializerHandler;
import com.machinelinking.pagestruct.WikiTextSerializerHandlerFactory;
import com.machinelinking.parser.DocumentSource;
import com.machinelinking.parser.MultiWikiTextParserHandler;
import com.machinelinking.parser.ValidatingWikiTextParserHandler;
import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.parser.WikiTextParserHandler;
import com.machinelinking.serializer.Serializer;
import com.machinelinking.splitter.Splitter;
import com.machinelinking.splitter.WikiTextParserHandlerSplitter;
import com.machinelinking.wikimedia.BufferedWikiPageHandler;
import com.machinelinking.wikimedia.WikiAPIParser;
import com.machinelinking.wikimedia.WikiPage;
import com.machinelinking.wikimedia.WikimediaUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The <i>Wiki</i> processor facade.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiPipeline {

    private final WikiAPIParser apiParser = new WikiAPIParser();

    private final List<Extractor> extractors = new ArrayList<Extractor>();

    private final List<Splitter> splitters   = new ArrayList<Splitter>();

    private final BufferedWikiPageHandler bufferedAPIHandler = new BufferedWikiPageHandler();

    private boolean validate         = false;
    private boolean produceStructure = true;

    public WikiPipeline() {}

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isProduceStructure() {
        return produceStructure;
    }

    public void setProduceStructure(boolean produceStructure) {
        this.produceStructure = produceStructure;
    }

    public boolean addExtractor(Extractor e) {
        return extractors.add(e);
    }

    public boolean removeExtractor(Extractor e) {
        return extractors.remove(e);
    }

    public List<Extractor> getExtractors() {
        return Collections.unmodifiableList(extractors);
    }

    public boolean addSplitter(Splitter s) {
        return splitters.add(s);
    }

    public boolean removeSplitter(Splitter s) {
        return splitters.remove(s);
    }

    public List<Splitter> getSplitters() {
        return Collections.unmodifiableList(splitters);
    }

    public void enrichEntity(DocumentSource source, Serializer serializer)
    throws SAXException, IOException, WikiTextParserException, InterruptedException, ExecutionException {
        try {
            serializer.openObject();

            serializer.fieldValue(Ontology.TYPE_FIELD, Ontology.TYPE_ENRICHED_ENTITY);

            // Write Document Serialization.
            writeDocumentSerialization(source, serializer);

            // Write extractors serialization.
            for (Extractor extractor : extractors) {
                serializer.field(extractor.getName());
                extractor.flushContent(serializer);
            }

            // Write splitters serialization.
            for(Splitter splitter : splitters) {
                splitter.serialize(serializer);
            }

            serializer.closeObject();
        } finally {
            serializer.close();
        }
    }

    private void writeDocumentSerialization(DocumentSource source, Serializer serializer)
    throws IOException, SAXException, WikiTextParserException {
        final InputStream wikiTextInputStream;
        final WikiPage wikiPage;
        // TODO: this logic should be moved in DocumentSource. Introduce PageMetadata.
        if(source.getInputStream() != null) {
            wikiTextInputStream = source.getInputStream();
            wikiPage = null;
        } else {
            final URL wikiAPIRequest = WikimediaUtils.entityToWikiTextURLAPI(source.getDocumentURL());
            bufferedAPIHandler.reset();
            apiParser.parse(bufferedAPIHandler, wikiAPIRequest.openStream());
            wikiPage = bufferedAPIHandler.getPage(true);
            if(wikiPage == BufferedWikiPageHandler.EOQ)
                throw new IOException(String.format("Error while parsing response body for request [%s]", wikiAPIRequest));
            wikiTextInputStream = new ByteArrayInputStream(wikiPage.getContent().getBytes());
        }

        // Writing page metadata.
        if(wikiPage != null) {
            serializer.fieldValue(Ontology.ID_FIELD, wikiPage.getId());
            serializer.fieldValue(Ontology.REVID_FIELD, wikiPage.getRevId());
            serializer.fieldValue(Ontology.SIZE_FIELD, wikiPage.getSize());
            serializer.fieldValue(Ontology.TITLE_FIELD, wikiPage.getTitle());
        }

        final WikiTextSerializerHandler serializerHandler =
                WikiTextSerializerHandlerFactory.getInstance().createSerializerHandler(serializer);
        final MultiWikiTextParserHandler multiHandler = new MultiWikiTextParserHandler();
        if(produceStructure) {
            multiHandler.add(serializerHandler);
        }
        for(Extractor extractor : extractors) { // Adding specific extractors.
            extractor.reset();
            multiHandler.add( wrapWithValidator("validator-" + extractor.getName(), extractor) );
        }

        final WikiTextParserHandlerSplitter handlerSplitter = new WikiTextParserHandlerSplitter();
        // NOTE: the handlerSplitter must be notified before the Splitters.
        multiHandler.add( handlerSplitter.getProxy() );
        for(Splitter splitter : splitters) {
            splitter.reset();
            splitter.initHandlerSplitter(handlerSplitter);
            multiHandler.add( wrapWithValidator("splitter-" + splitter.getName(), splitter) );
        }

        final WikiTextParser wikiTextParser = new WikiTextParser( wrapWithValidator("parser", multiHandler) );
        if(produceStructure) {
            serializer.field(Ontology.PAGE_DOM_FIELD);
            serializer.openList();
         }
        wikiTextParser.parse(
                source.getDocumentURL(),
                wikiTextInputStream
        );
        if(produceStructure) {
            serializer.closeList();
        }
    }

    private WikiTextParserHandler wrapWithValidator(String name, WikiTextParserHandler h) {
        if( isValidate() ) {
            return new ValidatingWikiTextParserHandler(name, h).getProxy();
        } else {
            return h;
        }
    }

}
