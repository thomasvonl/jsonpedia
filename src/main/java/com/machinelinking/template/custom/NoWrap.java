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
public class NoWrap implements TemplateCallHandler {

    public static final String NOWRAP_TEMPLATE_NAME = "nowrap";

    @Override
    public boolean process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        try {
            if (!NOWRAP_TEMPLATE_NAME.equals(context.evaluate(call.getName()))) return false;
            try {
                for (TemplateCall.Parameter p : call.getParameters()) {
                    writer.text(context.evaluate(p.value));
                }
                return true;
            } catch (IOException ioe) {
                throw new TemplateCallHandlerException("Error while processing nowrap template", ioe);
            }
        } catch (NodeRenderException nre) {
            throw new TemplateCallHandlerException("Error while evauating context.", nre);
        }
    }
}
