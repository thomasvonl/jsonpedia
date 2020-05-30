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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific {@link Extractor} for <i>Wikipedia reference</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ReferenceExtractor extends SectionAwareExtractor {

    private URL documentURL;

    private List<Reference> references;

    private StringBuilder referenceContent = new StringBuilder();
    private boolean foundParam;

    public ReferenceExtractor() {
        super(Ontology.REFERENCES_FIELD);
    }

    @Override
    public void beginDocument(URL document) {
        documentURL = document;
    }

    @Override
    public void beginReference(String label) {
        referenceContent.delete(0, referenceContent.length());
        foundParam = false;
    }

    @Override
    public void parameter(String param) {
        if(foundParam) referenceContent.append("|");
        foundParam = true;
        if(param != null) {
            referenceContent.append(param).append("=");
        }
    }

    @Override
    public void text(String content) {
        referenceContent.append(content);
    }

    @Override
    public void endReference(String label) {
        if(references == null) references = new ArrayList<>();
        try {
            references.add(new Reference(documentURL, label, referenceContent.toString(), super.getSectionIndex()));
        } catch (MalformedURLException murle) {
            throw new RuntimeException("Error while building reference.", murle);
        }
    }

    @Override
    public void flushContent(Serializer serializer) {
        if(references == null) {
            serializer.value(null);
            return;
        }
        serializer.openList();
        for(Reference reference : references) {
            reference.serialize(serializer);
        }
        serializer.closeList();
        references.clear();
    }

    @Override
    public void reset() {
        if(references != null) references.clear();
    }

}
