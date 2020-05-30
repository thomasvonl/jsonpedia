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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Annotation wrapper for {@link com.machinelinking.pipeline.Flag}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@XmlRootElement(name = "quota-overflow")
public class QuotaOverflowMessage {

    private final long quota;

    public QuotaOverflowMessage(long quota) {
        this.quota = quota;
    }

    private QuotaOverflowMessage() {
        this(0);
    }

    @XmlElement
    public String getMessage() {
        return String.format("Quota reached, please retry in %d ms.", quota);
    }

    @XmlElement
    public long getWaitTime() {
        return quota;
    }

}
