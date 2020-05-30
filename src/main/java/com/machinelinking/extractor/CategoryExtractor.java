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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link Extractor} for categories.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class CategoryExtractor extends Extractor {

    public static final String CATEGORY_PREFIX = "Category:";

    private final Set<String> categories = new HashSet<>();

    public CategoryExtractor() {
        super(Ontology.CATEGORIES_FIELD);
    }

    @Override
    public void beginReference(String label) {
        if(label != null && label.startsWith(CATEGORY_PREFIX)) {
            categories.add(label.substring(CATEGORY_PREFIX.length()));
        }
    }

    @Override
    public void flushContent(Serializer serializer) {
        final String[] sortedCategories = categories.toArray( new String[categories.size()] );
        Arrays.sort(sortedCategories);
        serializer.openObject();
        serializer.field("content");
        serializer.openList();
        for(String category : sortedCategories) {
            serializer.value(category);
        }
        serializer.closeList();
        serializer.closeObject();
    }

    @Override
    public void reset() {
        categories.clear();
    }

}
