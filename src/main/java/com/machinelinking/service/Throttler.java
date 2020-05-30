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

/**
 * Manages the throttling time for a generic {@link com.machinelinking.service.Service} access.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface Throttler {

    /**
     * Verifies the waiting time (in milliseconds) for a given IP address.
     *
     * @param ip IP address to be verified.
     * @return <code>0</code> if no longer wait, <code>w > 0</code> milliseconds waiting time if still waiting.
     */
    long checkAllowed(String ip);

}
