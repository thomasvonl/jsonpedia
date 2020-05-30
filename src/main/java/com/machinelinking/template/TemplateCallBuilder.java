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

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builder for template call.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateCallBuilder {

    private static  final TemplateCallBuilder builder = new TemplateCallBuilder();

    public static TemplateCallBuilder getInstance() {
        return builder;
    }

    private TemplateCallBuilder() {}

    public TemplateCall buildCall(JsonNode node) {
        final JsonNode name = node.get(Ontology.NAME_FIELD);
        final JsonNode content = node.get(Ontology.CONTENT_FIELD);
        final List<TemplateCall.Parameter> parameters = new ArrayList<>();
        String paramName;
        for(Map.Entry<String,JsonNode> entry : JSONUtils.toIterable(content.getFields())) {
            paramName = entry.getKey().startsWith(Ontology.ANON_NAME_PREFIX) ? null : entry.getKey();
            parameters.add(new TemplateCall.Parameter(paramName, simplifySingleElemArray(entry.getValue())));
        }
        return new DefaultTemplateCall(
                name,
                parameters.toArray( new TemplateCall.Parameter[parameters.size()])
        );
    }

    //TODO: remove when introduced serialization with single array elem avoidance.
    private JsonNode simplifySingleElemArray(JsonNode e) {
        if(e.isArray() && e.size() == 1) {
            return e.get(0);
        } else {
            return e;
        }
    }

}
