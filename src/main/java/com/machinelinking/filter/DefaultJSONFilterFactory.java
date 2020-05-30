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
 * Default implementation of {@link com.machinelinking.filter.JSONFilterFactory}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONFilterFactory implements JSONFilterFactory {

    public static final JSONFilter EMPTY_FILTER = new JSONFilter() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void setNested(JSONFilter nested) {
            throw new IllegalStateException();
        }

        @Override
        public JSONFilter getNested() {
            return null;
        }

        @Override
        public String humanReadable() {
            return "<empty>";
        }
    };

    @Override
    public JSONFilter createEmptyFilter() {
        return EMPTY_FILTER;
    }

    @Override
    public JSONObjectFilter createJSONObjectFilter() {
        return new DefaultJSONObjectFilter();
    }

    @Override
    public JSONKeyFilter createJSONJsonKeyFilter() {
        return new DefaultJSONKeyFilter();
    }

}
