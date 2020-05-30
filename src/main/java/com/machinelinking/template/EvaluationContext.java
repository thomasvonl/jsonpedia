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
import com.machinelinking.render.DefaultHTMLWriter;
import com.machinelinking.render.HTMLWriter;
import com.machinelinking.render.JsonContext;
import com.machinelinking.render.NodeRenderException;
import com.machinelinking.render.RootRender;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the evaluation context for a {@link TemplateCall}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class EvaluationContext {

    private final RootRender rootRender;
    private final JsonContext jsonContext;

    public EvaluationContext(RootRender rootRender, JsonContext jsonContext) {
        this.rootRender = rootRender;
        this.jsonContext = jsonContext;
    }

    public JsonContext getJsonContext() {
        return jsonContext;
    }

    public String getVarValue(String name) {
        return jsonContext.getDocumentContext().getVar(name);
    }

    public void evaluate(JsonNode node, StringBuilder sb) throws NodeRenderException {
        if(node.isArray()) {
            evaluate((ArrayNode) node, sb);
        } else if(node.isObject()) {
            evaluate((ObjectNode) node, sb);
        } else {
            sb.append(node.asText());
        }
    }

    public String evaluate(JsonNode node) throws NodeRenderException {
        if(node == null || node.isNull()) return null;
        final StringBuilder sb = new StringBuilder();
        evaluate(node, sb);
        return sb.toString();
    }

    public Map<String,String> evaluate(TemplateCall.Parameter[] parameters, int from) throws NodeRenderException {
        final Map<String,String> result = new HashMap<>();
        String name, value;
        for (int i = from; i < parameters.length; i++) {
            name = parameters[i].name;
            value = evaluate(parameters[i].value);
            if(name == null) {
                name = value;
                value = null;
            }
            result.put(name, value);
        }
        return result;
    }

    public void evaluate(String field, JsonNode value, HTMLWriter writer) throws NodeRenderException {
        rootRender.render(this.jsonContext, rootRender, field, value, writer);
    }

    private void evaluate(ArrayNode array, StringBuilder sb) throws NodeRenderException {
        for(JsonNode elem : array) {
            evaluate(elem, sb);
        }
    }

    private void evaluate(ObjectNode obj, StringBuilder sb) throws NodeRenderException {
        final String type = obj.get(Ontology.TYPE_FIELD).asText();
        if(Ontology.TYPE_VAR.equals(type)) {
            final String name = obj.get(Ontology.NAME_FIELD).asText();
            final JsonNode defaultValue = obj.get(Ontology.DEFAULT_FIELD);
            final String varValue = getVarValue(name);
            final String value = varValue != null ? varValue : evaluate(defaultValue);
            if(value != null) sb.append(value);
        } else {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final OutputStreamWriter osw = new OutputStreamWriter(baos);
            final HTMLWriter writer = new DefaultHTMLWriter(osw);
            try {
                rootRender.render(jsonContext, rootRender, obj, writer);
                writer.flush();
                sb.append(baos.toString());
            } catch (IOException ioe) {
                throw new NodeRenderException(ioe);
            }
        }
    }

}
