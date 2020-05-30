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

import com.machinelinking.template.DefaultTemplateProcessorFactory;
import com.machinelinking.template.EvaluationContext;
import com.machinelinking.template.TemplateCall;
import com.machinelinking.template.TemplateCallBuilder;
import com.machinelinking.template.TemplateProcessor;
import com.machinelinking.template.TemplateProcessorException;
import org.codehaus.jackson.JsonNode;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplateNodeRender implements NodeRender {

    private final TemplateProcessor processor;

    public TemplateNodeRender(TemplateProcessor processor) {
        this.processor = processor;
    }

    public TemplateNodeRender() {
        this( DefaultTemplateProcessorFactory.getInstance().createProcessor() );
    }

    @Override
    public boolean acceptNode(JsonContext context, JsonNode node) {
        return true;
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer) {
        final TemplateCall call = TemplateCallBuilder.getInstance().buildCall(node);
        final EvaluationContext evaluationContext = new EvaluationContext(rootRender, context);
        try {
            processor.process(evaluationContext, call, writer);
        } catch (TemplateProcessorException tpe) {
            throw new RuntimeException(tpe);
        }
    }
}
