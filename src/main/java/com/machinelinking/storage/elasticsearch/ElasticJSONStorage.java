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
import com.machinelinking.storage.JSONStorage;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexMissingException;


/**
 * {@link com.machinelinking.storage.JSONStorage} implementation for <i>ElasticSearch</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticJSONStorage
    implements JSONStorage<ElasticJSONStorageConfiguration, ElasticDocument, ElasticSelector> {

    private final ElasticJSONStorageConfiguration configuration;

    private final DocumentConverter<ElasticDocument> converter;

    public ElasticJSONStorage(
            ElasticJSONStorageConfiguration configuration,
            DocumentConverter<ElasticDocument> converter
    ) {
        this.configuration = configuration;
        this.converter = converter;
    }

    @Override
    public ElasticJSONStorageConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public DocumentConverter<ElasticDocument> getConverter() {
        return converter;
    }

    @Override
    public boolean exists() {
        return exists(null);
    }

    @Override
    public boolean exists(String collection) {
        final String targetCollection = collection == null ? configuration.getCollection() : collection;
        final ElasticJSONStorageConnection connection = openConnection(targetCollection);
        return connection.existsCollection();
    }

    @Override
    public ElasticJSONStorageConnection openConnection() {
        return openConnection(null);
    }

    @Override
    public ElasticJSONStorageConnection openConnection(String collection) {
        final String targetCollection = collection == null ? configuration.getCollection() : collection;
        final TransportClient client = new TransportClient();
        client.addTransportAddress(
                new InetSocketTransportAddress(
                        getConfiguration().getHost(),
                        getConfiguration().getPort()
                )
        );
        return new ElasticJSONStorageConnection(
                client,
                getConfiguration().getDB(),
                targetCollection,
                converter
        );

    }

    @Override
    public void deleteCollection() {
        deleteCollection(null);
    }

    @Override
    public void deleteCollection(String collection) {
        final String targetCollection = collection == null ? configuration.getCollection() : collection;
        final ElasticJSONStorageConnection connection = openConnection(targetCollection);
        try {
            connection.dropCollection();
        } catch (IndexMissingException ime) {
            // Pass.
        }
    }

    @Override
    public void close() {
        // Empty.
    }

}
