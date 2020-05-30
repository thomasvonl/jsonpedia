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

import com.machinelinking.storage.JSONStorageConnectionException;
import com.machinelinking.util.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link com.machinelinking.storage.elasticsearch.ElasticJSONStorage} test for <i>ElasticSearch</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticJSONStorageTest extends ElasticJSONStorageTestBase {

    private Logger logger = Logger.getLogger(ElasticJSONStorageTest.class);

    @Test
    public void testAddGetRemove() throws JSONStorageConnectionException {
        final ElasticJSONStorage storage = createStorage();
        final ElasticJSONStorageConnection connection = storage.openConnection(TEST_COLLECTION);

        final int id = Integer.MAX_VALUE;
        final String uuid = UUID.randomUUID().toString();
        final String KEY = "rand_uuid";
        final Map<String,Object> wData = new HashMap<>();
        wData.put(KEY, uuid);

        connection.addDocument(new ElasticDocument(id, 1, "test_rw", wData));
        connection.flush();

        final Map<String,Object> rData = connection.getDocument(id).getContent();
        Assert.assertEquals(rData.get(KEY).toString(), uuid);

        connection.removeDocument(id);
        Assert.assertNull(connection.getDocument(id));
    }

    @Test
    public void testLoad() throws IOException, JSONStorageConnectionException {
        final ElasticJSONStorage storage = super.createStorage();

        storage.deleteCollection();

        final Map<String,?> data = JSONUtils.parseJSONAsMap(
                IOUtils.toString(this.getClass().getResourceAsStream("/com/machinelinking/pipeline/Page1.json"))
        );

        long start = 0;
        try (final ElasticJSONStorageConnection connection = storage.openConnection(TEST_COLLECTION)
        ) {
            try {
                connection.getDocumentsCount();
                Assert.fail("Expected exception due connection deletion.");
            } catch (JSONStorageConnectionException e) {
                // OK.
            }

            final int SIZE = 1000;
            start = System.currentTimeMillis();
            for (int i = 0; i < SIZE; i++) {
                connection.addDocument(new ElasticDocument(i, i, "doc_" + i, data));
            }

            for (int i = 0; i < SIZE; i++) {
                Assert.assertEquals(connection.getDocument(i).getName(), "doc_" + i);
            }
            Assert.assertEquals(SIZE, connection.getDocumentsCount());
        } finally {
            logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
        }
    }

}
