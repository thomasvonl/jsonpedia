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

import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class FreebaseKeyValueRender implements KeyValueRender {

    private static final Map<String,String> TYPE_DIV_ATTR = new HashMap<String,String>(){{
        put("class", "freebase");
    }};

    @Override
    public void render(JsonContext context, RootRender rootRender, String key, JsonNode value, HTMLWriter writer)
    throws NodeRenderException {
        try {
            writer.openTag("div");

            writer.heading(3, "Freebase");

            final String id = value.get("id").asText();
            final String mid = value.get("mid").asText();
            final String name = value.get("name").asText();
            writer.openTag("pre");
            writer.text(name);
            writer.closeTag();

            writer.openTag("pre");
            writer.text(mid);
            writer.closeTag();

            writer.anchor(
                    String.format("http://www.freebase.com/view/%s", id),
                    name,
                    false
            );

            writer.heading(4, "Types");
            final JsonNode notable = value.get("notable");
            writer.openTag("span", TYPE_DIV_ATTR);
            writer.anchor(
                    String.format("http://www.freebase.com/view/%s", notable.get("id").asText()),
                    notable.get("name").asText(),
                    false
            );
            writer.closeTag();

            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }
}
