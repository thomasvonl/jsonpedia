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

package com.machinelinking.storage.mongodb;

import com.machinelinking.storage.AbstractJSONStorageFactory;
import com.machinelinking.storage.DocumentConverter;

import java.net.UnknownHostException;

/**
 * Mongo implementation of {@link com.machinelinking.storage.JSONStorageFactory}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoJSONStorageFactory extends AbstractJSONStorageFactory<MongoJSONStorageConfiguration, MongoJSONStorage, MongoDocument> {

    @Override
    public MongoJSONStorageConfiguration createConfiguration(String configURI) {
        final Configuration c = parseConfigurationURI(configURI);
        return new MongoJSONStorageConfiguration(c.host, c.port, c.db, c.collection);
    }

    @Override
    public MongoJSONStorage createStorage(
            MongoJSONStorageConfiguration config, DocumentConverter<MongoDocument> converter
    ) {
        try {
            return new MongoJSONStorage(config, converter);
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException("Error while instantiating storage with configuration: " + config);
        }
    }

    @Override
    public MongoJSONStorage createStorage(MongoJSONStorageConfiguration config) {
        return createStorage(config, null);
    }

}
