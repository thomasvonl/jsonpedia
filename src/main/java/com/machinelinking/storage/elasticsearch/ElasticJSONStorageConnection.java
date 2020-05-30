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

import com.machinelinking.storage.DocumentConverter;
import com.machinelinking.storage.JSONStorageConnection;
import com.machinelinking.storage.JSONStorageConnectionException;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.WikiPage;
import org.codehaus.jackson.util.TokenBuffer;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;

import java.io.IOException;

/**
 * {@link com.machinelinking.storage.JSONStorageConnection} implementation for <i>ElasticSearch</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticJSONStorageConnection implements JSONStorageConnection<ElasticDocument, ElasticSelector> {

    private final Client client;
    private final String db;
    private final String collection;
    private final DocumentConverter<ElasticDocument> converter;

    protected ElasticJSONStorageConnection(
            Client client, String db, String collection, DocumentConverter<ElasticDocument> converter
    ) {
        this.client = client;
        this.db = db;
        this.collection = collection;
        this.converter = converter;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public ElasticDocument createDocument(WikiPage page, TokenBuffer buffer) throws JSONStorageConnectionException {
        try {
            return new ElasticDocument(page.getId(), page.getRevId(), page.getTitle(), JSONUtils.parseJSONAsMap(buffer));
        } catch (IOException ioe) {
            throw new JSONStorageConnectionException("Error while creating document.", ioe);
        }
    }

    @Override
    public void addDocument(ElasticDocument in) {
        final ElasticDocument document = converter == null ? in : converter.convert(in);
        client
            .prepareIndex(db, collection, Integer.toString(document.getId()))
            .setSource(document.getInternal())
            .execute().actionGet();
    }

    @Override
    public void removeDocument(int id) {
        client.prepareDelete(db, collection, Integer.toString(id)).execute().actionGet();
    }

    @Override
    public ElasticDocument getDocument(int id) {
        final GetResponse response = client.prepareGet(db, collection, Integer.toString(id))
            .execute()
            .actionGet();
        return ElasticDocument.unwrap(response.getSource());
    }

    @Override
    public long getDocumentsCount() throws JSONStorageConnectionException {
        final String index = getIndex(db, collection);
        try {
            return client.prepareCount(index).execute().get().getCount();
        } catch (Exception e) {
            throw new JSONStorageConnectionException(
                    String.format("Error while counting documents in index [%s]", index), e
            );
        }
    }

    @Override
    public ElasticResultSet query(ElasticSelector selector, int limit) throws JSONStorageConnectionException {
        final String index = getIndex(db, collection);
        try {
            SearchRequestBuilder requestBuilder =
                    client
                    .prepareSearch(index)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            final QueryBuilder query = selector.buildQuery();
            requestBuilder.setQuery(query);
            requestBuilder.setFrom(0).setSize(limit).setExplain(false);
            final SearchResponse response = requestBuilder.execute().actionGet();
            return new ElasticResultSet(requestBuilder.toString(), response);
        } catch (Exception e) {
            throw new JSONStorageConnectionException("Error while waiting for response.", e);
        }
    }

    public String facetQuery(String facetIndex, String qry) throws JSONStorageConnectionException {
        final SearchRequestBuilder builder = new SearchRequestBuilder(client);
        builder.setIndices(facetIndex);
        builder.setSource(qry);
        final SearchRequest request = builder.request();
        final SearchResponse response = client.search(request).actionGet();
        return response.toString();
    }

    @Override
    public void flush() {
        // Empty.
    }

    @Override
    public void close() {
        client.close();
    }

    protected boolean existsCollection() {
        final String index = getIndex(db, collection);
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }

    protected void dropCollection() {
        final String index = getIndex(db, collection);
        final DeleteIndexResponse response = client.admin().indices().prepareDelete(index).execute().actionGet();
        if(!response.isAcknowledged())
            throw new IllegalStateException(String.format("Cannot delete index [%s]", index));
    }

    private String getIndex(String db, String collection) {
        //return String.format("%s-%s", db, collection);
        return db;
    }

}
