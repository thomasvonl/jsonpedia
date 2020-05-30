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
import java.util.Iterator;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class SectionsKeyValueRender implements KeyValueRender {

    @Override
    public void render(JsonContext context, RootRender rootRender, String key, JsonNode value, HTMLWriter writer)
    throws NodeRenderException {
        final StringBuilder sb = new StringBuilder();
        final Iterator<JsonNode> sections = value.getElements();
        JsonNode section;
        sb.append("<pre>");
        while (sections.hasNext()) {
            section = sections.next();
            toTabulation(section.get("level").asInt(), sb);
            final String title = section.get("title").asText();
            sb.append(String.format("<a href=\"#%s\">", SectionRender.toAnchorName(title)));
            sb.append(title);
            sb.append("</a>");
            sb.append("\n");
        }
        sb.append("</pre>");

        try {
            writer.openTag("div");
            writer.heading(1, "Sections");
            writer.html(sb.toString());
            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

    private void toTabulation(int t, StringBuilder sb) {
        for(int i = 0; i < t; i++) {
            sb.append("\t");
        }
    }

}
