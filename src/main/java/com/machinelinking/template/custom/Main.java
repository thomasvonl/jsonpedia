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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Main implements TemplateCallHandler {

    public static final String MAIN_TEMPLATE_NAME = "[Mm]ain";

    private static final Map<String,String> MAIN_DIV_ATTR = new HashMap<String,String>(){{
        put("class", "main");
    }};

    @Override
    public boolean process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        try {
            if(! context.evaluate(call.getName()).matches(MAIN_TEMPLATE_NAME)) return false;
        } catch (NodeRenderException nre) {
            throw new TemplateCallHandlerException("Error while evaluating context.", nre);
        }

        try {
            writer.openTag("span", MAIN_DIV_ATTR);
            writer.openTag("strong");
            writer.text("Main Article: ");
            writer.closeTag();
            for(String article : call.getParameterNames()) {
                final String domain = context.getJsonContext().getDocumentContext().getDocumentURL().getHost();
                writer.anchor(String.format("http://%s/wiki/%s", domain, article), article, true);
                writer.text(" ");
            }
            writer.closeTag();
            return true;
        } catch (IOException ioe) {
            throw new TemplateCallHandlerException("Error while invoking Main", ioe);
        }
    }
}
