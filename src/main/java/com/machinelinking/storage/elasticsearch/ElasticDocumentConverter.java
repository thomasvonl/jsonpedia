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

package com.machinelinking.storage.elasticsearch;

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.splitter.InfoboxSplitter;
import com.machinelinking.splitter.TableSplitter;
import com.machinelinking.storage.DocumentConverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
class ElasticDocumentConverter implements DocumentConverter<ElasticDocument> {

    final Set<String> blocked = new HashSet<>();

    ElasticDocumentConverter() {
        blocked.add(Ontology.PAGE_DOM_FIELD);
        blocked.add(InfoboxSplitter.NAME);
        blocked.add(TableSplitter.NAME);
    }

    @Override
    public ElasticDocument convert(ElasticDocument in) {
        final Map<String,Object> content = in.getContent();
        final Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, ?> entry : content.entrySet()) {
            if (blocked.contains(entry.getKey())) continue;
            out.put(entry.getKey(), entry.getValue());
        }
        return new ElasticDocument(in.getId(), in.getVersion(), in.getName(), out);
    }
}
