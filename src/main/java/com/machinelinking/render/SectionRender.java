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
public class SectionRender implements NodeRender {

    public static String toAnchorName(String title) {
        return title.toLowerCase().replaceAll(" ", "_");
    }

    private static final Map<String,String> SECTION_DIV_ATTR = new HashMap<String,String>(){{
        put("class", "section");
    }};

    public static final String TITLE = "title";
    public static final String LEVEL = "level";

    @Override
    public boolean acceptNode(JsonContext context, JsonNode node) {
        return true;
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer)
    throws NodeRenderException {
        final String title = node.get(TITLE).asText();
        int level = node.get(LEVEL).asInt() + 1;
        try {
            writer.anchor(toAnchorName(title));
            writer.openTag("div", SECTION_DIV_ATTR);
            writer.heading(level, title);
            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

}
