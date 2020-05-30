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

import com.machinelinking.parser.Attribute;
import com.machinelinking.parser.AttributeScanner;

/**
 * Default {@link JSONFilterParser} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONFilterParser implements JSONFilterParser {

    public static final char NESTING_SEPARATOR     = '>';
    public static final char CONSTRAINT_SEPARATOR  = ',';
    public static final char FIELD_SEPARATOR       = ':';
    public static final char FIELD_VALUE_DELIMITER = '"';

    @Override
    public JSONFilter parse(String exp, JSONFilterFactory factory) {
        if(exp == null || exp.trim().length() == 0) return factory.createEmptyFilter();

        final String[] levels = exp.split(Character.toString(NESTING_SEPARATOR));
        JSONFilter current;
        JSONFilter top = null;
        JSONFilter prev = null;
        for(String level : levels) {
            if(level.trim().length() == 0)
                throw new IllegalArgumentException(String.format("Invalid selector content [%s].", level));
            if(!level.contains(Character.toString(FIELD_SEPARATOR))) {
                JSONKeyFilter keyFilter = factory.createJSONJsonKeyFilter();
                parseKeyFilter(level, keyFilter);
                current = keyFilter;
            } else {
                JSONObjectFilter objectFilter = factory.createJSONObjectFilter();
                parseObjectFilter(level, objectFilter);
                current = objectFilter;
            }
            if(top == null) top = current;
            if(prev != null) prev.setNested(current);
            prev = current;
        }
        return top;
    }

    private void parseKeyFilter(String level, JSONKeyFilter newFilter) {
        newFilter.setCriteria(level);
    }

    private void parseObjectFilter(String level, JSONObjectFilter newFilter) {
        final Attribute[] attributes = AttributeScanner.scan(
                CONSTRAINT_SEPARATOR, FIELD_SEPARATOR, FIELD_VALUE_DELIMITER, level
        );
        for(Attribute attribute : attributes) {
            newFilter.addCriteria(attribute.name, attribute.value);
        }
    }
}
