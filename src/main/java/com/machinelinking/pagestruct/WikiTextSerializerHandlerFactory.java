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

package com.machinelinking.pagestruct;

import com.machinelinking.serializer.Serializer;

/**
 * Factory for {@link WikiTextSerializerHandler}s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextSerializerHandlerFactory {

    private static WikiTextSerializerHandlerFactory singleton;

    public static WikiTextSerializerHandlerFactory getInstance() {
        if(singleton == null) singleton = new WikiTextSerializerHandlerFactory();
        return singleton;
    }

    public WikiTextSerializerHandler createSerializerHandler(Serializer serializer) {
        return new WikiTextSerializerHandler(serializer);
    }

    private WikiTextSerializerHandlerFactory() {}

}
