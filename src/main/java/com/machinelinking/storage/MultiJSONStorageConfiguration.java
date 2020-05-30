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

package com.machinelinking.storage;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiJSONStorageConfiguration implements JSONStorageConfiguration, Iterable<JSONStorageConfiguration> {

    private final JSONStorageConfiguration[] configurations;

    protected MultiJSONStorageConfiguration(JSONStorageConfiguration[] configurations) {
        if(configurations.length == 0) throw new IllegalArgumentException();
        this.configurations = configurations;
    }

    @Override
    public String getDB() {
        return getSample().getDB();
    }

    @Override
    public String getCollection() {
        return getSample().getCollection();
    }

    @Override
    public Iterator<JSONStorageConfiguration> iterator() {
        return Arrays.asList(configurations).iterator();
    }

    private JSONStorageConfiguration getSample() {
        return configurations[0];
    }
}
