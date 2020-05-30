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

package com.machinelinking.filter;

import org.codehaus.jackson.JsonNode;

/**
 * Defines a filter of an object satisfying a set of criteria.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface JSONObjectFilter extends JSONFilter {

    /**
     * Adds a filtering criteria based on exact matching.
     *
     * @param fieldName name of the field to match.
     * @param fieldPattern the pattern to match for the field value.
     */
    void addCriteria(String fieldName, String fieldPattern);

    /**
     * Returns the field pattern set for a field name.
     *
     * @param fieldName name of a field.
     * @return a regex.
     */
    String getCriteriaPattern(String fieldName);

    /**
     * @param node
     * @return <code>true</code> if match is satisfied, <code>false</code> otherwise.
     */
    boolean match(JsonNode node);
}
