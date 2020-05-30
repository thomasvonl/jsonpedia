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

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class IPA implements TemplateCallHandler {

    public static final String TEMPLATE_PATTERN = "IPA[c]?[-]?.{2,3}";

    @Override
    public boolean process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        try {
            if(!context.evaluate(call.getName()).matches(TEMPLATE_PATTERN)) return false;
            writer.openTag("i");
            writer.text("[");
            String value;
            for (TemplateCall.Parameter p : call.getParameters()) {
                value = context.evaluate(p.value);
                if("lang".equals(value) || value.endsWith(".ogg")) continue;
                writer.text(value);
            }
            writer.text("]");
            writer.closeTag();
        } catch (NodeRenderException | IOException e) {
            throw new TemplateCallHandlerException("Error while evaluating IPA", e);
        }
        return true;
    }

}
