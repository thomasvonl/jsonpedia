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

import com.machinelinking.storage.AbstractJSONStorageLoaderTest;
import com.machinelinking.storage.JSONStorage;
import org.testng.annotations.Test;

import java.net.UnknownHostException;

/**
 * {@link com.machinelinking.storage.elasticsearch.ElasticJSONStorage} test.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@Test
public class ElasticJSONStorageLoaderTest extends AbstractJSONStorageLoaderTest {

    public static final int SERVICE_PORT = 9300;

    @Override
    protected JSONStorage getJSONStorage() throws UnknownHostException {
        return new ElasticJSONStorage(
                new ElasticJSONStorageConfiguration("localhost", SERVICE_PORT, TEST_STORAGE_DB, TEST_STORAGE_COLLECTION),
                new ElasticDocumentConverter()
        );
    }

}
