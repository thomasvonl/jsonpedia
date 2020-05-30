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

package com.machinelinking.storage.elasticsearch;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class ElasticJSONStorageTestBase {

    public static int TEST_PORT = 9300;
    public static String TEST_DB = "jsonpedia_test_db";
    public static String TEST_COLLECTION = "test_collection";

    public ElasticJSONStorage createStorage(int port, String db, String collection) {
        final ElasticJSONStorageFactory factory = new ElasticJSONStorageFactory();
        return factory.createStorage(
                factory.createConfiguration(
                        String.format("localhost:%d:%s:%s", port, db, collection)
                )
        );
    }

    public ElasticJSONStorage createStorage() {
        return createStorage(TEST_PORT, TEST_DB, TEST_COLLECTION);
    }

}
