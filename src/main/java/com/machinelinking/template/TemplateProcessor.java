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

package com.machinelinking.template;

import com.machinelinking.render.HTMLWriter;

/**
 * Defines a processor for a template.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface TemplateProcessor {

    public void process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateProcessorException;

    void addTemplateCallHandler(String scope, TemplateCallHandler handler);

    void removeTemplateCallHandler(String scope, TemplateCallHandler handler);

}
