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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default {@link JSONFilterEngine} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONFilterEngine implements JSONFilterEngine {

    public static JSONFilter parseFilter(String exp, JSONFilterFactory factory) {
        final JSONFilterParser parser = new DefaultJSONFilterParser();
        return parser.parse(exp, factory);
    }

    public static JSONFilter parseFilter(String exp) {
        return parseFilter(exp, new DefaultJSONFilterFactory());
    }

    public static JsonNode[] applyFilter(JsonNode node, String exp) {
        final JSONFilter filter = parseFilter(exp);
        final JSONFilterEngine engine = new DefaultJSONFilterEngine();
        return engine.filter(node, filter);
    }

    public static JsonNode[] applyFilter(JsonNode node, JSONFilter filter) {
        if(filter.isEmpty()) return new JsonNode[]{node};
        final JSONFilterEngine engine = new DefaultJSONFilterEngine();
        return engine.filter(node, filter);
    }

    @Override
    public JsonNode[] filter(JsonNode in, JSONFilter filter) {
        final List<JsonNode> result = new ArrayList<>();
        filterNode(in, filter, result);
        return result.toArray( new JsonNode[result.size()] );
    }

    private List<JsonNode> selectNode(JsonNode t, JSONFilter f) {
        final List<JsonNode> r = new ArrayList<>();
        if(f instanceof JSONKeyFilter) {
            final JSONKeyFilter keyFilter = (JSONKeyFilter) f;
            final Iterator<Map.Entry<String,JsonNode>> entries = t.getFields();
            while(entries.hasNext()) {
                final Map.Entry<String,JsonNode> entry = entries.next();
                if(keyFilter.matchKey(entry.getKey())) {
                    r.add(entry.getValue());
                }
            }
        } else if(f instanceof JSONObjectFilter) {
            final JSONObjectFilter objectFilter = (JSONObjectFilter) f;
            if(objectFilter.match(t)) {
                r.add(t);
            }
        }
        return r;
    }

    private void filterNode(JsonNode n, JSONFilter f, List<JsonNode> r) {
        if(n.isObject()) {
            final List<JsonNode> matched = selectNode(n, f);
            if(!matched.isEmpty()) {
                final JSONFilter nested = f.getNested();
                if(nested == null) {
                    r.addAll(matched);
                } else {
                    final List<JsonNode> candidate = new ArrayList<>();
                    for(JsonNode m : matched) {
                        filterObject(m, nested, candidate);
                    }
                    r.addAll(candidate);
                }
            } else {
                filterObject(n, f, r);
            }
        } else if(n.isArray()) {
            filterArray(n, f, r);
        }
    }

    private void filterObject(JsonNode n, JSONFilter f, List<JsonNode> r) {
        final Iterator<JsonNode> elements = n.getElements();
        while (elements.hasNext()) {
            filterNode(elements.next(), f, r);
        }
    }

    private void filterArray(JsonNode n, JSONFilter f, List<JsonNode> r) {
        final int size = n.size();
        for(int i = 0; i < size; i++) {
            filterNode(n.get(i), f, r);
        }
    }

}
