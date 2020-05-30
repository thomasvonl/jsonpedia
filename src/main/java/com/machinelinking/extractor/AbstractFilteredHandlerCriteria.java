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

import com.machinelinking.parser.FilteredHandlerCriteria;

/**
 * Defines a {@link FilteredHandlerCriteria to extract abstracts}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class AbstractFilteredHandlerCriteria implements FilteredHandlerCriteria {

    public static final FilteredHandlerCriteria INSTANCE = new AbstractFilteredHandlerCriteria();
    public static final FilteredHandlerCriteria NOT_ABSTRACT_INSTANCE = new FilteredHandlerCriteria() {
        @Override
        public boolean mustFilter(int paragraphIndex, int sectionLevel, int nestingLevel, boolean plainTextFound) {
            return !INSTANCE.mustFilter(paragraphIndex, sectionLevel, nestingLevel, plainTextFound);
        }
    };

    @Override
    public boolean mustFilter(
            int paragraphIndex, int sectionLevel, int nestingLevel, boolean plainTextFound
    ) {
        return !plainTextFound || sectionLevel != -1;
    }

}
