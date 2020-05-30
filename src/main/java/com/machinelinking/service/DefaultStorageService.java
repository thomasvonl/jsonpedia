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

import com.machinelinking.filter.DefaultJSONFilterEngine;
import com.machinelinking.filter.JSONFilter;
import com.machinelinking.storage.JSONStorageConnectionException;
import com.machinelinking.storage.elasticsearch.ElasticDocument;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageConfiguration;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageConnection;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageFactory;
import com.machinelinking.storage.elasticsearch.ElasticResultSet;
import com.machinelinking.storage.elasticsearch.ElasticSelector;
import com.machinelinking.storage.elasticsearch.ElasticSelectorParser;
import com.machinelinking.storage.mongodb.MongoDocument;
import com.machinelinking.storage.mongodb.MongoJSONStorage;
import com.machinelinking.storage.mongodb.MongoJSONStorageConfiguration;
import com.machinelinking.storage.mongodb.MongoJSONStorageConnection;
import com.machinelinking.storage.mongodb.MongoJSONStorageFactory;
import com.machinelinking.storage.mongodb.MongoResultSet;
import com.machinelinking.storage.mongodb.MongoSelector;
import com.machinelinking.storage.mongodb.MongoSelectorParser;
import com.machinelinking.storage.mongodb.MongoUtils;
import com.machinelinking.util.JSONUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;

/**
 * Default implementation of {@link com.machinelinking.service.StorageService}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@Path("/storage")
public class DefaultStorageService extends ServiceBase implements StorageService {

    public static final String STORAGE_SERVICE_CONNECTION_MONGO_PROP = "storage.service.connection.mongo";
    public static final String STORAGE_SERVICE_CONNECTION_ELASTIC_PROP = "storage.service.connection.elastic";
    public static final String STORAGE_SERVICE_QUERY_LIMIT_PROP = "storage.service.query.limit";
    public static final String STORAGE_SERVICE_ELASTIC_FACETING_DB_PROP = "storage.service.elastic.faceting.db";

    private static final Logger logger = Logger.getLogger(DefaultStorageService.class);

    private static final MongoSelector EMPTY_SELECTOR = new MongoSelector();

    private final MongoJSONStorageConnection mongoConnection;
    private final ElasticJSONStorageConnection elasticConnection;

    private final String STORAGE_SERVICE_ELASTIC_FACETING_DB;

    private final int QUERY_LIMIT;

    public DefaultStorageService() {
        final ConfigurationManager manager = ConfigurationManager.getInstance();
        QUERY_LIMIT = Integer.parseInt(manager.getProperty(STORAGE_SERVICE_QUERY_LIMIT_PROP));
        STORAGE_SERVICE_ELASTIC_FACETING_DB = manager.getProperty(STORAGE_SERVICE_ELASTIC_FACETING_DB_PROP);
        mongoConnection = initMongoConnection(manager);
        elasticConnection = initElasticConnection(manager);
    }

    MongoJSONStorageConnection initMongoConnection(ConfigurationManager manager) {
        final String connString = manager.getProperty(STORAGE_SERVICE_CONNECTION_MONGO_PROP);
        final MongoJSONStorageFactory factory = new MongoJSONStorageFactory();
        final MongoJSONStorageConfiguration configuration = factory.createConfiguration(connString);
        final MongoJSONStorage storage = factory.createStorage(configuration);
        return storage.openConnection(configuration.getCollection());
    }

    ElasticJSONStorageConnection initElasticConnection(ConfigurationManager manager) {
        final String connString = manager.getProperty(STORAGE_SERVICE_CONNECTION_ELASTIC_PROP);
        final ElasticJSONStorageFactory factory = new ElasticJSONStorageFactory();
        final ElasticJSONStorageConfiguration configuration = factory.createConfiguration(connString);
        final ElasticJSONStorage storage = factory.createStorage(configuration);
        return storage.openConnection(configuration.getCollection());
    }

    @Path("/mongo/select")
    @GET
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
    })
    @Override
    public Response queryMongoStorage(
            @QueryParam("q") String selector,
            @QueryParam("filter") String filter,
            @QueryParam("limit") String limit
    ) {
        super.checkQuota();
        try {
            selector = trimIfNotNull(selector);
            filter = trimIfNotNull(filter);
            assertParam(selector, "selector parameter must be specified");

            final MongoSelector mongoSelector = MongoSelectorParser.getInstance().parse(selector);
            final JSONFilter jsonFilter = DefaultJSONFilterEngine.parseFilter(filter);
            try (final MongoResultSet rs = mongoConnection.query(mongoSelector, toMaxLimit(limit))) {
                return Response.ok(
                        toMongoSelectJSONOutput(mongoSelector, jsonFilter, rs),
                        MediaType.APPLICATION_JSON + ";charset=UTF-8"
                ).build();
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidRequestException(iae);
        } catch (Exception e) {
            logger.error("Error while processing request.", e);
            throw new InternalErrorException(e);
        }
    }

    @Path("/mongo/mapred")
    @GET
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
    })
    @Override
    public Response mapRedMongoStorage(
            @QueryParam("criteria") String criteria,
            @QueryParam("map")String map,
            @QueryParam("reduce")String reduce,
            @QueryParam("limit") String limit
    ) {
        super.checkQuota();
        try {
            criteria = trimIfNotNull(criteria);
            map = trimIfNotNull(URLDecoder.decode(map, "utf8"));
            reduce = trimIfNotNull(URLDecoder.decode(reduce, "utf8"));

            assertParam(map, "map param must be specified");
            assertParam(reduce, "reduce param must be specified");

            final MongoSelector mongoSelector;
            if(criteria == null || criteria.trim().length() == 0) {
                mongoSelector = EMPTY_SELECTOR;
            } else {
                mongoSelector = MongoSelectorParser.getInstance().parse(criteria);
            }

            final JsonNode result = mongoConnection.processMapReduce(
                    mongoSelector.toDBObjectSelection(), map, reduce, toMaxLimit(limit)
            );
            return Response.ok(
                    toMongoMapRedJSONOutput(mongoSelector, result),
                    MediaType.APPLICATION_JSON + ";charset=UTF-8"
            ).build();
        } catch (IllegalArgumentException iae) {
            throw new InvalidRequestException(iae);
        } catch (Exception e) {
            logger.error("Error while processing request.", e);
            throw new InternalErrorException(e);
        }
    }

    @Path("/elastic/select")
    @GET
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
    })
    @Override
    public Response queryElasticStorage(
            @QueryParam("q") String selector,
            @QueryParam("filter") String filter,
            @QueryParam("limit") String limit
    ) {
        super.checkQuota();
        try {
            selector = trimIfNotNull(selector);
            filter = trimIfNotNull(filter);
            assertParam(selector, "selector parameter must be specified");

            final ElasticSelector elasticSelector = ElasticSelectorParser.getInstance().parse(selector);
            final JSONFilter jsonFilter = DefaultJSONFilterEngine.parseFilter(filter);
            try (final ElasticResultSet rs = elasticConnection.query(elasticSelector, toMaxLimit(limit))) {
                return Response.ok(
                        toElasticSelectJSONOutput(jsonFilter, rs),
                        MediaType.APPLICATION_JSON + ";charset=UTF-8"
                ).build();
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidRequestException(iae);
        } catch (Exception e) {
            logger.error("Error while processing request.", e);
            throw new InternalErrorException(e);
        }
    }

    @Path("/elastic/facet")
    @GET
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
    })
    @Override
    public Response queryElasticFacets(
            @QueryParam("callback") String callback,
            @QueryParam("source") String source
    ) {
        super.checkQuota();
        try {
            return Response.ok(
                    String.format(
                            "%s(%s);",
                            callback,
                            elasticConnection.facetQuery(STORAGE_SERVICE_ELASTIC_FACETING_DB, source)
                    ),
                    MediaType.APPLICATION_JSON + ";charset=UTF-8"
            ).build();
        } catch (JSONStorageConnectionException jsce) {
            throw new InvalidRequestException(jsce);
        }
    }

    private String trimIfNotNull(String in) {
        return in == null ? null : in.trim();
    }

    private void assertParam(Object val, String errMsg) {
        if(val == null) throw new IllegalArgumentException(errMsg);
    }

    private int toMaxLimit(String limit) {
        if(limit == null || limit.length() == 0) return QUERY_LIMIT;
        try {
            final int l = Integer.parseInt(limit);
            return l <= QUERY_LIMIT ? l : QUERY_LIMIT;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe);
        }
    }

    private JsonNode applyFilter(JsonNode target, JSONFilter filter) {
        if(filter == null) return target;
        final ArrayNode result = JsonNodeFactory.instance.arrayNode();
        for(JsonNode node : DefaultJSONFilterEngine.applyFilter(target, filter)) {
            result.add(node);
        }
        return result;
    }

    private String toMongoSelectJSONOutput(MongoSelector selector, JSONFilter filter, MongoResultSet rs) {
        final ObjectNode output = JSONUtils.getJsonNodeFactory().objectNode();
        final ArrayNode result = JSONUtils.getJsonNodeFactory().arrayNode();
        output.put("query-explain", selector.toString());
        output.put("mongo-selection", selector.toDBObjectSelection().toString());
        output.put("mongo-projection", selector.toDBObjectProjection().toString());
        output.put("count", rs.getCount());
        output.put("result", result);
        MongoDocument nextMongo;
        JsonNode nextJack;
        while((nextMongo = rs.next()) != null) {
            nextJack = MongoUtils.convertToJsonNode(nextMongo.getContent());
            result.add(applyFilter(nextJack, filter));
        }
        return JSONUtils.serializeToJSON(output, false);
    }

    private String toMongoMapRedJSONOutput(MongoSelector selector, JsonNode result) {
        final ObjectNode output = JSONUtils.getJsonNodeFactory().objectNode();
        output.put("query-explain", selector.toString());
        output.put("mongo-selection", selector.toDBObjectSelection().toString());
        output.put("count", result.size());
        output.put("result", result);
        return JSONUtils.serializeToJSON(output, false);
    }

    private String toElasticSelectJSONOutput(JSONFilter filter, ElasticResultSet rs) {
        final ObjectNode output = JSONUtils.getJsonNodeFactory().objectNode();
        final ArrayNode result = JSONUtils.getJsonNodeFactory().arrayNode();
        output.put("elastic-query", rs.getExplain());
        output.put("count", rs.getCount());
        output.put("result", result);
        ElasticDocument nextElastic;
        JsonNode nextJack;
        while((nextElastic = rs.next()) != null) {
            nextJack = JSONUtils.toJsonNode(nextElastic.getContent());
            result.add(applyFilter(nextJack, filter));
        }
        return JSONUtils.serializeToJSON(output, false);
    }

}
