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

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ContentKeyValueRender implements KeyValueRender {

    @Override
    public void render(JsonContext context, RootRender rootRender, String key, JsonNode value, HTMLWriter writer)
    throws NodeRenderException {
        try {
            if (value.isNull()) {
                writer.openTag("i");
                writer.text("&lt;null&gt;");
                writer.closeTag();
            } else if ((value.isArray() || value.isObject()) && value.size() == 0) {
                writer.openTag("i");
                writer.text("&lt;empty&gt;");
                writer.closeTag();
            } else {
                rootRender.render(context, rootRender, "Content", value, writer);
            }
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

}
