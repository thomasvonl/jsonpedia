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

import com.machinelinking.parser.Attribute;
import com.machinelinking.parser.AttributeScanner;
import com.machinelinking.storage.Criteria;
import com.machinelinking.storage.SelectorParser;
import com.machinelinking.storage.SelectorParserException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticSelectorParser implements SelectorParser {

    public static final char CRITERIA_SEPARATOR = ' ';
    public static final char FIELD_VALUE_SEPARATOR = ':';
    public static final char CRITERIA_DELIMITER = '"';

    private static ElasticSelectorParser instance;

    public static ElasticSelectorParser getInstance() {
        if(instance == null)
            instance = new ElasticSelectorParser();
        return instance;
    }

    private ElasticSelectorParser() {}

    @Override
    public ElasticSelector parse(String qry) throws SelectorParserException {
        final Attribute[] attributes = AttributeScanner.scan(
                CRITERIA_SEPARATOR, FIELD_VALUE_SEPARATOR, CRITERIA_DELIMITER, qry
        );
        final ElasticSelector selector = new ElasticSelector();
        for(Attribute attribute : attributes) {
            selector.addCriteria(new Criteria(attribute.name, Criteria.Operator.eq, attribute.value));
        }
        return selector;
    }
}
