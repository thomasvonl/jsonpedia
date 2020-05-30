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

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for {@link JSONFilter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONObjectFilter implements JSONObjectFilter {

    private final Map<String,String> criterias = new HashMap<>();

    private JSONFilter nested;

    public void setNested(JSONFilter nested) {
        if(this.nested != null) throw new IllegalStateException("Nested filter already set.");
        this.nested = nested;
    }

    @Override
    public void addCriteria(String fieldName, String fieldPattern) {
        if(criterias.containsKey(fieldName)) throw new IllegalArgumentException();
        criterias.put(fieldName, fieldPattern);
    }

    @Override
    public String getCriteriaPattern(String fieldName) {
        return criterias.get(fieldName);
    }

    @Override
    public boolean match(JsonNode node) {
        for(Map.Entry<String,String> criteria : criterias.entrySet()) {
            final JsonNode value = node.get(criteria.getKey());
            if(value == null) return false;
            if(criteria.getValue() == null) continue;
            if(!value.asText().matches(criteria.getValue())) return false;
        }
        return true;
    }

    @Override
    public JSONFilter getNested() {
        return nested;
    }

    @Override
    public boolean isEmpty() {
        return criterias.isEmpty();
    }

    @Override
    public String humanReadable() {
        final StringBuilder sb = new StringBuilder();
        sb.append("object_filter(");
        for(Map.Entry<String,String> criteria : criterias.entrySet()) {
            sb.append(criteria.getKey()).append('=').append(criteria.getValue()).append(',');
        }
        sb.append(')');
        sb.append('>').append(nested == null ? null : nested.humanReadable());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return 2 * criterias.hashCode() * 3 * (nested == null ? 1 : nested.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null) return false;
        if(!(obj instanceof DefaultJSONObjectFilter)) return false;
        final DefaultJSONObjectFilter other = (DefaultJSONObjectFilter) obj;
        return
            this.criterias.equals(other.criterias)
                &&
            (this.nested == null ? other.nested == null : this.nested.equals(other.nested));
    }

    @Override
    public String toString() {
        return humanReadable();
    }

}
