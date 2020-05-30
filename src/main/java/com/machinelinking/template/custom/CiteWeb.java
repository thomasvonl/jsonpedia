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

package com.machinelinking.template.custom;

import com.machinelinking.render.HTMLWriter;
import com.machinelinking.render.NodeRenderException;
import com.machinelinking.template.EvaluationContext;
import com.machinelinking.template.TemplateCall;
import com.machinelinking.template.TemplateCallHandler;
import com.machinelinking.template.TemplateCallHandlerException;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <i>Cite Web</i> template render.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class CiteWeb implements TemplateCallHandler {

    public static final String CITE_WEB_TEMPLATE_NAME = "cite web";

    private static final Map<String,String> CITEWEB_DIV_ATTR = new HashMap<String,String>(){{
        put("class", "cite-web");
    }};

    public static final String[] TEMPLATE_FIELDS = new String[] {
            "title", "publisher", "author", "last", "work", "archivedate", "accessdate", "url", "archiveurl",
    };

    @Override
    public boolean process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        if(! CITE_WEB_TEMPLATE_NAME.equals(call.getName().asText())) return false;
        try{
            writer.text("<!---- BEGIN---->");
            writer.openTag("span", CITEWEB_DIV_ATTR);
            writer.openTable("Cite Web", null);
            JsonNode value;
            for(String field: TEMPLATE_FIELDS) {
                value = call.getParameter(field);
                if(value == null) continue;
                writer.openTag("tr");
                writer.openTag("td");
                try {
                    context.evaluate(field, value, writer);
                } catch (NodeRenderException nre) {
                    throw new TemplateCallHandlerException("Error while evaluating context.", nre);
                }
                writer.closeTag();
                writer.closeTag();
            }
            writer.closeTable();
            writer.closeTag();
            writer.text("<!---- END---->");
            return true;
        } catch (IOException ioe) {
            throw new TemplateCallHandlerException("Error while invoking Cite Web", ioe);
        }
    }
}
