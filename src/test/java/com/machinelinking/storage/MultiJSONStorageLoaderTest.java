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

import java.net.UnknownHostException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiJSONStorageLoaderTest extends AbstractJSONStorageLoaderTest {

    public static final String MONGO_TEST_CONN_URI =
            String.format(
                    "com.machinelinking.storage.mongodb.MongoJSONStorageFactory|localhost:7654:%s:%s",
                    AbstractJSONStorageLoaderTest.TEST_STORAGE_DB, AbstractJSONStorageLoaderTest.TEST_STORAGE_COLLECTION
            );

    public static final String ELASTIC_TEST_CONN_URI =
            String.format(
                    "com.machinelinking.storage.elasticsearch.ElasticJSONStorageFactory|localhost:9300:%s:%s",
                    AbstractJSONStorageLoaderTest.TEST_STORAGE_DB, AbstractJSONStorageLoaderTest.TEST_STORAGE_COLLECTION
            );

    public static final String CONFIG_URI = MONGO_TEST_CONN_URI + ";" + ELASTIC_TEST_CONN_URI;

    @Override
    protected JSONStorage getJSONStorage() throws UnknownHostException {
        final MultiJSONStorageFactory factory = new MultiJSONStorageFactory();
        final MultiJSONStorageConfiguration config = factory.createConfiguration(CONFIG_URI);
        return factory.createStorage(config);
    }
}
