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

import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for text of page sections.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class SectionTextExtractor extends TextExtractor {

    private final List<String> sectionsTitle = new ArrayList<>();
    private final List<String> sectionsText = new ArrayList<>();

    private String lastSectionTitle;

    public SectionTextExtractor() {
        super(Ontology.SECTIONS_TEXT_FIELD, AbstractFilteredHandlerCriteria.NOT_ABSTRACT_INSTANCE);
    }

    @Override
    public void flushContent(Serializer serializer) {
        serializer.openList();
        for(int i = 0; i < sectionsText.size(); i++) {
            serializer.openObject();
            serializer.fieldValue(Ontology.TITLE_FIELD ,sectionsTitle.get(i));
            serializer.fieldValue(Ontology.CONTENT_FIELD ,sectionsText.get(i));
            serializer.closeObject();
        }
        serializer.closeList();
        sectionsTitle.clear();
        sectionsText.clear();
    }

    @Override
    public void section(String title, int level) {
        flushSection(title);
        super.section(title, level);
    }

    @Override
    public void endDocument() {
        flushSection(null);
        super.endDocument();
    }

    private void flushSection(String title) {
        sectionsTitle.add(lastSectionTitle);
        sectionsText.add(super.flushText());
        lastSectionTitle = title;
    }

}
