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

package com.machinelinking.service;

import com.machinelinking.storage.elasticsearch.ElasticJSONStorageTest;
import com.machinelinking.storage.mongodb.MongoJSONStorageTest;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Test case for {@link com.machinelinking.service.DefaultStorageService}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultStorageServiceTest extends ServiceTestBase {

    private static final String MAP_FUNC;
    private static final String RED_FUNC;

    static {
        try {
            MAP_FUNC = URLEncoder.encode(
                    "function() {  ocs = this.content.templates.occurrences; for(template in ocs) { emit(template, ocs[template]); } }",
                    "utf8"
            );
            RED_FUNC = URLEncoder.encode(
                    "function(key, values) { return Array.sum(values) }",
                    "utf8"
            );
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee);
        }
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();

        if(!ConfigurationManager.getInstance().isInitialized()) {
            final Properties properties = new Properties();
            properties.put(
                    DefaultStorageService.STORAGE_SERVICE_CONNECTION_MONGO_PROP,
                    String.format("localhost:7654:%s:%s",
                            MongoJSONStorageTest.TEST_DB, MongoJSONStorageTest.TEST_COLLECTION
                    )
            );
            properties.put(
                    DefaultStorageService.STORAGE_SERVICE_CONNECTION_ELASTIC_PROP,
                    String.format("localhost:9300:%s:%s",
                            ElasticJSONStorageTest.TEST_DB, ElasticJSONStorageTest.TEST_COLLECTION)
            );
            properties.put(DefaultStorageService.STORAGE_SERVICE_QUERY_LIMIT_PROP, Integer.toString(1000));
            properties.put(DefaultStorageService.STORAGE_SERVICE_ELASTIC_FACETING_DB_PROP, "jsonpedia_test_facet");
            ConfigurationManager.getInstance().initProperties(properties);
        }
    }

    @Test
    public void testMongoSelect() throws URISyntaxException, IOException, ConnectionException {
        final JsonNode output = performQuery(
            buildPath(DefaultStorageService.class, "mongo/select")
                    .queryParam("q", "name = doc_1 -> _id")
                    .queryParam("limit", Integer.toString(10))
                    .build()
        );

        Assert.assertEquals(
                output.get("query-explain").asText(),
                "criterias: [name eq 'doc_1'], projections: [content, _id, name, version]"
        );
        Assert.assertEquals(output.get("count").asInt(), 1);
        Assert.assertNotNull(output.get("result"));
        Assert.assertTrue(output.get("result").size() >= 1);
    }

    @Test
    public void testMongoSelectInvalidParams() throws URISyntaxException, IOException {
        performQueryAndCheckError(
                400,
                buildPath(DefaultStorageService.class, "mongo/select")
                        .queryParam("q", "bleah")
                        .queryParam("limit", "10")
                        .build()
        );

        performQueryAndCheckError(
                400,
                buildPath(DefaultStorageService.class, "mongo/select")
                        .queryParam("q", "name = doc_1 -> _id")
                        .queryParam("limit", "x")
                        .build()
        );
    }

    @Test
    public void testMongoMapRed() throws URISyntaxException, IOException, ConnectionException {
        final JsonNode output = performQuery(
            buildPath(DefaultStorageService.class, "mongo/mapred")
                    .queryParam("criteria", "")
                    .queryParam("map", MAP_FUNC)
                    .queryParam("reduce", RED_FUNC)
                    .queryParam("limit", Integer.toString(10))
                    .build()
        );

        Assert.assertTrue(output.get("result").size() >= 1);
    }

    @Test
    public void testMongoMapRedInvalidParams() throws URISyntaxException, IOException {
        performQueryAndCheckError(
                400,
                buildPath(DefaultStorageService.class, "mongo/mapred")
                        .queryParam("criteria", "X")
                        .queryParam("map", MAP_FUNC)
                        .queryParam("reduce", RED_FUNC)
                        .build()
        );

        performQueryAndCheckError(
                400,
                buildPath(DefaultStorageService.class, "mongo/mapred")
                        .queryParam("criteria", "")
                        .queryParam("map", MAP_FUNC)
                        .queryParam("reduce", RED_FUNC)
                        .queryParam("limit", "x")
                        .build()
        );

        performQueryAndCheckError(
                500,
                buildPath(DefaultStorageService.class, "mongo/mapred")
                        .queryParam("criteria", "")
                        .queryParam("map", MAP_FUNC)
                        .queryParam("reduce", "xxx")
                        .build()
        );

        performQueryAndCheckError(
                500,
                buildPath(DefaultStorageService.class, "mongo/mapred")
                        .queryParam("criteria", "")
                        .queryParam("map", "xxx")
                        .queryParam("reduce", RED_FUNC)
                        .build()
        );
    }

    @Test
    public void testElasticSelect() throws URISyntaxException, IOException, ConnectionException {
        final JsonNode output = performQuery(
            buildPath(DefaultStorageService.class, "elastic/select")
                    .queryParam("q", "_id:735 abstract:Albert")
                    .queryParam("limit", Integer.toString(10))
                    .build()
        );

        Assert.assertEquals(
                output.get("elastic-query").asText().replaceAll("\\s+", ""),
                "{\"from\":0,\"size\":10,\"query\":{\"bool\":{\"must\":[" +
                        "{\"match\":{\"_id\":{\"query\":\"735\",\"type\":\"boolean\"}}}," +
                        "{\"match\":{\"abstract\":{\"query\":\"Albert\",\"type\":\"boolean\"}}}]}}," +
                        "\"explain\":false}"
        );
        Assert.assertTrue(output.get("count").asInt() >= 1);
        Assert.assertNotNull(output.get("result"));
        Assert.assertTrue(output.get("result").size() >= 1);
    }

    @Test
    public void testElasticSelectInvalidParams() throws URISyntaxException, IOException {
        performQueryAndCheckError(
                400,
                buildPath(DefaultStorageService.class, "elastic/select")
                        .queryParam("limit", "x")
                        .build()
        );
    }

}
