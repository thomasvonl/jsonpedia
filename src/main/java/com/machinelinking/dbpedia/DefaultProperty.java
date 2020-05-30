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

package com.machinelinking.dbpedia;


import java.io.Serializable;

/**
 * Default implementation for {@link Property}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultProperty implements Property, Serializable {

    private final String property;

    private final String label;

    private final String domain;

    private final String range;

    public DefaultProperty(String property, String label, String domain, String range) {
        if(property == null) throw new NullPointerException();
        this.property = property;
        this.label = label;
        this.domain = domain;
        this.range = range;
    }

    @Override
    public String getPropertyName() {
        return property;
    }

    @Override
    public String getPropertyLabel() {
        return label;
    }

    @Override
    public String getPropertyDomain() {
        return domain;
    }

    @Override
    public String getPropertyRange() {
        return range;
    }

    @Override
    public String toString() {
        return String.format("{property=%s label='%s' domain=%s range=%s}", property, label, domain, range);
    }
}
