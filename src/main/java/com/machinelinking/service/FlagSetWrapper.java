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

package com.machinelinking.service;

import com.machinelinking.pipeline.Flag;
import com.machinelinking.pipeline.FlagSet;
import com.machinelinking.pipeline.WikiPipelineFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Annotation wrapper for {@link FlagWrapper}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@XmlRootElement
public class FlagSetWrapper implements FlagSet {

    private static FlagSetWrapper instance;

    public static FlagSetWrapper getInstance() {
        if(instance == null)
            instance = new FlagSetWrapper();
        return instance;
    }

    private final FlagWrapper[] definedFlagSet;
    private final FlagWrapper[] defaultFlagSet;

    private FlagSetWrapper() {
        Flag[] flags = WikiPipelineFactory.getInstance().getDefinedFlags();
        definedFlagSet = new FlagWrapper[flags.length];
        for(int i = 0; i < flags.length; i++) {
            definedFlagSet[i] = new FlagWrapper(flags[i]);
        }
        defaultFlagSet = new FlagWrapper[WikiPipelineFactory.DEFAULT_FLAGS.length];
        for(int i = 0; i < WikiPipelineFactory.DEFAULT_FLAGS.length; i++) {
            defaultFlagSet[i] = new FlagWrapper(WikiPipelineFactory.DEFAULT_FLAGS[i]);
        }
    }

    @XmlElement
    @Override
    public FlagWrapper[] getDefinedFlags() {
        return definedFlagSet;
    }

    @XmlElement
    @Override
    public FlagWrapper[] getDefaultFlags() {
        return defaultFlagSet;
    }

}
