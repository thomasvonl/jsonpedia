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

import com.machinelinking.freebase.FreebaseService;
import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.serializer.Serializer;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.WikimediaUtils;
import org.codehaus.jackson.JsonNode;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <i>Freebase</i> {@link Extractor}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class FreebaseExtractor extends Extractor {

    private Future<JsonNode> entityFetching;

    public FreebaseExtractor() {
        super(Ontology.FREEBASE_FIELD);
    }

    @Override
    public void beginDocument(URL document) {
        if(entityFetching != null) throw new IllegalStateException();
        final String entityName = WikimediaUtils.getEntityName(document.toExternalForm());
        entityFetching = fetchEntityData(entityName);
    }

    @Override
    public void flushContent(Serializer serializer) {
        try {
            final JsonNode freebaseData = entityFetching.get();
            JSONUtils.jacksonNodeToSerializer(freebaseData, serializer);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching entity.", e);
        }
    }

    @Override
    public void reset() {
        if(entityFetching != null) {
            entityFetching.cancel(true);
            entityFetching = null;
        }
    }

    private Future<JsonNode> fetchEntityData(final String entityName) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        return executorService.submit( new Callable<JsonNode>() {
            @Override
            public JsonNode call() throws Exception {
                return FreebaseService.getInstance().getEntityData(entityName);
            }
        });
    }

}
