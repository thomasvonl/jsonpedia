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

import com.machinelinking.extractor.Reference;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ReferenceNodeRender implements NodeRender {

    private static final String ALT_PREFIX = "alt=";

    private static final Map<String,String> REFERENCE_NODE_ATTR = new HashMap<String,String>(){{
        put("class", "reference");
    }};

    public static void writeReferenceHTML(String lang, String ref, String caption, HTMLWriter writer)
    throws IOException {
         writer.openTag("span", REFERENCE_NODE_ATTR);
        if(Reference.isImage(ref) ) {
            final String[] descSections = caption.split("\\|");
            writer.key(descSections[descSections.length -1]);
            String alt = "";
            for(String descSection : descSections) {
                if(descSection.startsWith(ALT_PREFIX)) {
                    alt = descSection.substring(ALT_PREFIX.length());
                    break;
                }
            }
            writer.image(Reference.imageResourceToURL(ref), alt);
        } else {
            writer.reference(caption, lang, ref, true);
        }
        writer.closeTag();
    }

    @Override
    public boolean acceptNode(JsonContext context, JsonNode node) {
        return true;
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer)
    throws NodeRenderException {
        final String label       = node.get("label").asText();
        final String description = node.get("content").asText().trim();
        try {
            writeReferenceHTML(
                    context.getLang(),
                    label,
                    description.length() == 0 ? label : description,
                    writer
            );
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

}
