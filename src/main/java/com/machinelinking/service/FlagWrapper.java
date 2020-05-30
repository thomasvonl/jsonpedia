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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Annotation wrapper for {@link Flag}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@XmlRootElement(name = "flag")
public class FlagWrapper implements Flag {

    private final Flag flag;

    public FlagWrapper(Flag flag) {
        this.flag = flag;
    }

    private FlagWrapper() {
        this(null);
    }

    @XmlElement
    @Override
    public String getId() {
        return flag.getId();
    }

    @XmlElement
    @Override
    public String getDescription() {
        return flag.getDescription();
    }

}
