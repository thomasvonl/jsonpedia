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

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Specific {@link Extractor} collecting occurrences of <i>Wikipedia Template</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateOccurrencesExtractor extends Extractor {

    private Map<String,Integer> templateOccurrences = new HashMap<>();

    public TemplateOccurrencesExtractor() {
        super(Ontology.TEMPLATES_FIELD);
    }

    @Override
    public void beginTemplate(TemplateName name) {
        final String n = name.plain.trim();
        Integer count = templateOccurrences.get(n);
        count = count == null ? 1 : count + 1;
        templateOccurrences.put(n, count);
    }

    @Override
    public void flushContent(Serializer serializer) {
        serializer.openObject();
        serializer.field("occurrences");
        serializer.openObject();
        for(Map.Entry<String,Integer> entry : templateOccurrences.entrySet()) {
            serializer.fieldValue(entry.getKey(), entry.getValue());
        }
        serializer.closeObject();
        serializer.closeObject();
    }

    @Override
    public void reset() {
        templateOccurrences.clear();
    }

}
