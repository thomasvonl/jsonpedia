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

package com.machinelinking.serializer;

/**
 * {@link DataEncoder} implementation for <i>MongoDB</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoDBDataEncoder implements DataEncoder {

    @Override
    public String encodeFieldName(String in) {
        return in.replaceAll("%", "%25").replaceAll("\\$", "%24").replaceAll("\\.", "%2e");
    }

    @Override
    public String encodeFieldValue(String in) {
        return in;
    }

}
