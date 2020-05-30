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

package com.machinelinking.template;

import org.codehaus.jackson.JsonNode;

/**
 * Models a template call.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface TemplateCall {

    JsonNode getName();

    Parameter[] getParameters();

    String[] getParameterNames();

    JsonNode getParameter(String param);

    JsonNode getParameter(int index);

    int getParametersCount();

    class Parameter {
        public final String name;
        public final JsonNode value;

        public Parameter(String name, JsonNode value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", name, value);
        }
    }

}
