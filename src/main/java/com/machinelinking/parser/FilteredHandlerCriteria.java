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

package com.machinelinking.parser;

/**
 * Defines a criteria for {@link com.machinelinking.parser.WikiTextParserFilteredHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface FilteredHandlerCriteria {

    /**
     * Defines whether  or not an event must be filtered on the basis of the current section and nesting levels.
     *
     * @param paragraphIndex index of the current paragraph (paragraphs are separated by <code>\n\n+</code>).
     * @param sectionLevel index of current section. Until first section index is <code>-1</code>.
     * @param nestingLevel index of event nesting inside other events.
     * @param plainTextFound <code>true</code> if plain text at nesting level <code>0</code> has been found.
     * @return <code>true</true> if the event must be filtered, <code>false</code> otherwise.
     */
    boolean mustFilter(int paragraphIndex, int sectionLevel, int nestingLevel, boolean plainTextFound);

}
