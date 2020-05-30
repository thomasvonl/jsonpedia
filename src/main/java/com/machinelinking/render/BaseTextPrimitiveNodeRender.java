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

package com.machinelinking.render;

import com.machinelinking.util.DefaultJsonPathBuilder;
import com.machinelinking.util.JsonPathBuilder;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class BaseTextPrimitiveNodeRender implements PrimitiveNodeRender {

    private final Map<String,String> TEXT_SPAN_ATTR = new HashMap<String,String>(){{
        put("class", "primitive");
    }};

    private final JsonPathBuilder targetFilter;

    public BaseTextPrimitiveNodeRender() {
        targetFilter = new DefaultJsonPathBuilder();
        targetFilter.startPath();
        targetFilter.enterObject();
        targetFilter.field("wikitext-json");
        targetFilter.enterArray();
        targetFilter.arrayElem();
        targetFilter.enterObject();
        targetFilter.field("structure");
        targetFilter.enterArray();
    }

    @Override
    public boolean render(JsonContext context, JsonNode node, HTMLWriter writer) throws IOException {
        if(context.subPathOf(targetFilter, true) && node.isTextual()) {
            writer.openTag("span", TEXT_SPAN_ATTR);
            writer.text(node.asText());
            writer.closeTag();
            return true;
        }
        return false;
    }

}
