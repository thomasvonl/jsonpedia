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

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateSuppressor implements TemplateCallHandler {

    private final String[] namePatterns;

    public TemplateSuppressor(String... namePatterns) {
        this.namePatterns = namePatterns;
    }

    @Override
    public boolean process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        final String name;
        try {
            name = context.evaluate(call.getName());
        } catch (NodeRenderException nre) {
            throw new TemplateCallHandlerException("Error while evaluating context.", nre);
        }
        for (String namePattern : namePatterns) {
            if (name.matches(namePattern)) return true;
        }
        return false;
    }

}
