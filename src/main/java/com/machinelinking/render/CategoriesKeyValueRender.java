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

import com.machinelinking.wikimedia.WikimediaUtils;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class CategoriesKeyValueRender implements KeyValueRender {

    @Override
    public void render(JsonContext context, RootRender rootRender, String key, JsonNode value, HTMLWriter writer)
    throws NodeRenderException {
        final Iterator<JsonNode> categories = value.get("content").getElements();
        try {
            writer.openTag("div");
            writer.heading(1, "Categories");
            final String lang = WikimediaUtils.urlToParts(context.getDocumentContext().getDocumentURL()).lang;
            while (categories.hasNext()) {
                writer.category(lang, categories.next().asText());
            }
            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

}
