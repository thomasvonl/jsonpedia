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

package com.machinelinking.splitter;

import com.machinelinking.parser.WikiPediaUtils;

/**
 * {@link Splitter} implementation for <i>infobox</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InfoboxSplitter extends Splitter {

    public static final String NAME = "infobox-splitter";

    public InfoboxSplitter() {
        super(NAME);
    }

    @Override
    public void beginTemplate(TemplateName name) {
        if(WikiPediaUtils.getInfoBoxName(name.plain) != null) {
            super.split();
        }
    }

}
