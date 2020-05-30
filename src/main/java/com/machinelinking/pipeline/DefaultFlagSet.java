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

package com.machinelinking.pipeline;

/**
 * Default {@link FlagSet} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultFlagSet implements FlagSet {

    private final Flag[] definedFlags;
    private final Flag[] defaultFlags;

    public DefaultFlagSet(Flag[] definedFlags, Flag[] defaultFlags) {
        this.definedFlags = definedFlags;
        this.defaultFlags = defaultFlags;
    }

    @Override
    public Flag[] getDefinedFlags() {
        return definedFlags;
    }

    @Override
    public Flag[] getDefaultFlags() {
        return defaultFlags;
    }

}
