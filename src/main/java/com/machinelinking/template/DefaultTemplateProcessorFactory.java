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

import com.machinelinking.template.custom.CiteBook;
import com.machinelinking.template.custom.CiteJournal;
import com.machinelinking.template.custom.CiteWeb;
import com.machinelinking.template.custom.IPA;
import com.machinelinking.template.custom.Main;
import com.machinelinking.template.custom.NoWrap;
import com.machinelinking.template.custom.TemplateSuppressor;

/**
 * Default implementation of {@link TemplateProcessorFactory}.
 * Registers custom handlers for templates.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultTemplateProcessorFactory implements TemplateProcessorFactory {

    private static final DefaultTemplateProcessorFactory instance = new DefaultTemplateProcessorFactory();

    public static final DefaultTemplateProcessorFactory getInstance() {
        return instance;
    }

    @Override
    public TemplateProcessor createProcessor() {
        final DefaultTemplateProcessor processor = new DefaultTemplateProcessor();
        processor.addTemplateCallHandler(null, new Main() );
        processor.addTemplateCallHandler(null, new IPA() );
        processor.addTemplateCallHandler(null, new NoWrap() );

        processor.addTemplateCallHandler(RenderScope.TEXT_RENDERING, new TemplateSuppressor(".*")); // Suppress all.

        processor.addTemplateCallHandler(RenderScope.FULL_RENDERING, new CiteWeb());
        processor.addTemplateCallHandler(RenderScope.FULL_RENDERING, new CiteBook());
        processor.addTemplateCallHandler(RenderScope.FULL_RENDERING, new CiteJournal());
        return processor;
    }

}
