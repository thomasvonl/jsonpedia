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

import com.machinelinking.storage.Criteria;
import com.machinelinking.storage.JSONStorageConnection;
import com.machinelinking.storage.JSONStorageConnectionException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

/**
 * Test case for {@link com.machinelinking.storage.mongodb.MongoJSONStorage}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoJSONStorageTest {

    public static String TEST_DB = "jsonpedia_test_db";
    public static String TEST_COLLECTION = "test_collection";

    private static final String MAP_FUNC = "function() {  ocs = this.content.templates.occurrences; for(template in ocs) { emit(template, ocs[template]); } }";
    private static final String RED_FUNC = "function(key, values) { return Array.sum(values) }";

    private static Logger logger = Logger.getLogger(MongoJSONStorageTest.class);

    @Test
    public void testAddGetRemove() throws JSONStorageConnectionException {
        final MongoJSONStorage storage = getStorage();
        final MongoJSONStorageConnection connection = storage.openConnection(TEST_COLLECTION);

        final int id = Integer.MAX_VALUE;
        final String uuid = UUID.randomUUID().toString();
        final String KEY = "rand_uuid";
        final DBObject wData = new BasicDBObject(KEY, uuid);

        connection.addDocument(new MongoDocument(id, 1, "test_rw", wData));
        connection.flush();

        final DBObject rData = connection.getDocument(id).getContent();
        Assert.assertEquals(rData.get(KEY).toString(), uuid);

        connection.removeDocument(id);
        Assert.assertNull(connection.getDocument(id));
    }

    // 10000 items with save(): 90s
    // 10000 items with insert(): 80s
    @Test
    public void testLoad() throws IOException {
        loadAndCheck(10);
    }

    @Test
    public void testQuery() throws IOException, JSONStorageConnectionException {
        loadAndCheck(10);

        final MongoJSONStorage storage = getStorage();
        final JSONStorageConnection connection = storage.openConnection(TEST_COLLECTION);
        final MongoSelector selector = new MongoSelector();
        selector.addCriteria( new Criteria("version", Criteria.Operator.lte, 0));
        selector.addCriteria( new Criteria("content.categories.content", Criteria.Operator.eq, "Cosmologists"));
        selector.addCriteria( new Criteria("content.sections.title", Criteria.Operator.eq, "Biography"));
        selector.addProjection("_id");
        selector.addProjection("name");
        selector.addProjection("content");
        final MongoResultSet rs = (MongoResultSet) connection.query(selector, 1000);
        int found = 0;
        while((rs.next()) != null) {
            found++;
        }
        Assert.assertEquals(found, 1);
    }

    @Test
    public void testMapReduce() {
        final MongoJSONStorage storage = getStorage();
        final MongoJSONStorageConnection connection = storage.openConnection(TEST_COLLECTION);
        final JsonNode result = connection.processMapReduce(new BasicDBObject(), MAP_FUNC, RED_FUNC, 0);
        Assert.assertTrue(result.isArray());
        Assert.assertTrue(result.size() > 50);
    }

    private void loadAndCheck(final int count) throws IOException {
        final MongoJSONStorage storage = getStorage();
        storage.deleteCollection(TEST_COLLECTION);
        final DBObject dbNode = (DBObject) JSON.parse(
                IOUtils.toString(this.getClass().getResourceAsStream("/com/machinelinking/pipeline/Page1.json"))
        );
        long start = 0;
        long end   = 0;
        try (final MongoJSONStorageConnection connection = storage.openConnection(TEST_COLLECTION)
        ) {
            Assert.assertEquals(connection.getDocumentsCount(), 0);
            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                connection.addDocument(new MongoDocument(i, i, "doc_" + i, dbNode));
            }
            end = System.currentTimeMillis();
            connection.flush();
            Assert.assertEquals(connection.getDocumentsCount(), count);
        } finally {
            if(end == 0) end = System.currentTimeMillis();
            logger.info("Elapsed time: " + (end - start));
        }
    }

    private MongoJSONStorage getStorage() {
        final MongoJSONStorageFactory factory = new MongoJSONStorageFactory();
        return factory.createStorage(
                factory.createConfiguration(String.format("localhost:7654:%s:%s", TEST_DB, TEST_COLLECTION))
        );
    }

}
