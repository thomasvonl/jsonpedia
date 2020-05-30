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
import com.machinelinking.dbpedia.TemplateMappingFactory;
import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.parser.WikiPediaUtils;
import com.machinelinking.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Extracts <i>DBpedia Mapping</i>s for <i>Wikipedia Template</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateMappingExtractor extends Extractor {

    private final ExecutorService executorService               = Executors.newCachedThreadPool();
    private final List<Future> waitList                         = new ArrayList<>();
    private final Map<String,TemplateMapping> collectedMappings = new HashMap<>();

    public TemplateMappingExtractor() {
        super(Ontology.TEMPLATE_MAPPING_FIELD);
    }

    public Map<String,TemplateMapping> getCollectedMappings() throws ExecutionException, InterruptedException {
        waitMappings();
        return Collections.unmodifiableMap(collectedMappings);
    }

    @Override
    public void beginTemplate(TemplateName name) {
        if(WikiPediaUtils.getInfoBoxName(name.plain) != null) {
            fetchMapping(name.plain.trim());
        }
    }

    @Override
    public void flushContent(Serializer serializer) {
        try {
            waitMappings();
        } catch (Exception e) {
            throw new RuntimeException("Error while waiting for mappings.", e);
        }
        writeEntityMappingJSONSerialization(collectedMappings, serializer);
    }

    @Override
    public void reset() {
        clearWaitList();
        collectedMappings.clear();
    }

    private void fetchMapping(final String mappingName) {
        final Future future = executorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final TemplateMapping mapping =
                                    TemplateMappingFactory.getInstance().readMappingForTemplate(mappingName);
                            if(mapping != null) collectedMappings.put(mappingName, mapping);
                        } catch (Exception e) {
                            throw new RuntimeException("Error while fetching mapping " + mappingName, e);
                        }
                    }
                }
        );
        waitList.add(future);
    }

    private void waitMappings() throws InterruptedException, ExecutionException {
        for (Future future : waitList) {
            future.get();
        }
    }

    private void clearWaitList() {
        for(Future future : waitList) {
            future.cancel(true);
        }
        waitList.clear();
    }

    private void writeEntityMappingJSONSerialization(
        Map<String,TemplateMapping> collectedMappings, Serializer serializer
    ) {
        serializer.openObject();
        serializer.field("mapping-collection");
        serializer.openList();
        for(Map.Entry<String,TemplateMapping> entry : collectedMappings.entrySet()) {
            entry.getValue().serialize(serializer);
        }
        serializer.closeList();
        serializer.closeObject();
    }

}
