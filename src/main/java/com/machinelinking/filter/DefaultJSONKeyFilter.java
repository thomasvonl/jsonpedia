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

/**
 * Default {@link com.machinelinking.filter.JSONKeyFilter} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONKeyFilter implements JSONKeyFilter {

    private String criteria;
    private JSONFilter nested;

    public DefaultJSONKeyFilter() {}

    public void setCriteria(String c) {
        if(this.criteria != null) throw new IllegalStateException();
        this.criteria = c;
    }

    public void setNested(JSONFilter n) {
        if(this.nested != null) throw new IllegalStateException();
        this.nested = n;
    }

    @Override
    public boolean matchKey(String key) {
        return criteria.matches(key);
    }

    @Override
    public JSONFilter getNested() {
        return nested;
    }

    @Override
    public boolean isEmpty() {
        return criteria == null;
    }

    @Override
    public String humanReadable() {
        return String.format("key_filter(%s)>%s", criteria, nested == null ? null : nested.humanReadable());
    }

    @Override
    public String toString() {
        return humanReadable();
    }
}
