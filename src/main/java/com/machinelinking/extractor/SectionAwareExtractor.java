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

import java.net.URL;

/**
 * Base {@link Extractor} to manage <i>Wikimedia Section</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class SectionAwareExtractor extends Extractor {

    private short sectionIndex;
    private String sectionTitle;

    protected SectionAwareExtractor(String name) {
        super(name);
    }

    public short getSectionIndex() {
        return sectionIndex;
    }

    public String getSectionTitle() { return sectionTitle; }

    public boolean insideHeader() {
        return getSectionIndex() == -1;
    }

    @Override
    public void beginDocument(URL document) {
        sectionIndex = -1; // -1 == page header.
    }

    @Override
    public void section(String title, int level) {
        sectionTitle = title;
        sectionIndex++;
    }

}
