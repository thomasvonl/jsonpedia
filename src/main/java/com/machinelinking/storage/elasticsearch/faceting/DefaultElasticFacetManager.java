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

package com.machinelinking.storage.elasticsearch.faceting;

import com.machinelinking.storage.JSONStorageConnectionException;
import com.machinelinking.storage.elasticsearch.ElasticDocument;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageConnection;
import com.machinelinking.storage.elasticsearch.ElasticResultSet;
import com.machinelinking.storage.elasticsearch.ElasticSelector;
import com.machinelinking.util.JSONUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.IndexMissingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManagerConfiguration.Analyzer;
import static com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManagerConfiguration.Property;

/**
 * Default implementation of {@link com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultElasticFacetManager implements ElasticFacetManager {

    public static final String SOURCE_CONFIG_FILE = "source-config.json";

    private final ElasticFacetManagerConfiguration configuration;

    public static Map<String,Object> toPropertyDefinition(final Property p) {
        return new HashMap<String,Object>(){{
            if(p.analyzer == Analyzer.not_analyzed) {
                put("index", "not_analyzed");
            } else {
                put("analyzer", p.analyzer.toValue());
            }
            put("type", p.type.toValue());
        }};
    }

    public static void setMappings(Property[] properties, Map<String,?> source) {
        final Map<String,List<Property>> fieldToProps = aggregateByField(properties);
        final Map<String,Object> mappings = (Map<String,Object>) source.get("mappings");
        for(Map.Entry<String,List<Property>> fieldToPropsEntry : fieldToProps.entrySet()) {
            final Map<String,Object> typeProperties = new HashMap<>();
            final String field = fieldToPropsEntry.getKey();
            final String indexName = String.format("%s_index", field);
            mappings.put(indexName, typeProperties);
            Map typeMapping = new HashMap();
            typeProperties.put("properties", typeMapping);
            final List<Property> currentProperties = fieldToPropsEntry.getValue();
            if(currentProperties.size() == 1) { // Single property per index.
                final Property property = currentProperties.get(0);
                typeMapping.put(property.field, toPropertyDefinition(property));
            } else { // Multi property
                final Map<String,Object> multiProps = new HashMap<>();
                for(Property property : currentProperties) {
                    multiProps.put(property.analyzer.toValue(), toPropertyDefinition(property));
                }
                final Map<String,Object> multiField = new HashMap<>();
                multiField.put("fields", multiProps);
                multiField.put("type", "multi_field");
                typeMapping.put(field, multiField);
            }
        }
    }

    public static Map<String,Object> setMappings(Property[] properties) {
        final Map<String, Object> sourceTemplate;
        try {
            sourceTemplate = (Map<String, Object>) JSONUtils.parseJSONAsMap(
                    DefaultElasticFacetManager.class.getResourceAsStream(SOURCE_CONFIG_FILE)
            );
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        setMappings(properties, sourceTemplate);
        return sourceTemplate;
    }

    private static Map<String,List<Property>> aggregateByField(Property[] properties) {
        final Map<String,List<Property>> result = new HashMap<>();
        for(Property property : properties) {
            List<Property> l = result.get(property.field);
            if(l == null) {
                l = new ArrayList<>();
                result.put(property.field, l);
            }
            l.add(property);
        }
        return result;
    }

    public DefaultElasticFacetManager(ElasticFacetManagerConfiguration configuration) {
        this.configuration = configuration;
        initStorage(configuration);
    }

    @Override
    public ElasticFacetManagerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public FacetLoadingReport loadFacets(ElasticSelector selector, FacetConverter converter)
    throws JSONStorageConnectionException {
        final ElasticJSONStorage inStorage = configuration.getSourceStorage();
        final ElasticJSONStorage outStorage = configuration.getDestinationStorage();
        ElasticDocument document;
        int processedDocs = 0;
        int generatedFacetDocs = 0;
        try(
            ElasticJSONStorageConnection readConnection = inStorage.openConnection();
            ElasticResultSet rs = readConnection.query(selector, configuration.getLimit());
            ElasticJSONStorageConnection writeConnection = outStorage.openConnection()
        ) {
            while ((document = rs.next()) != null) {
                for (ElasticDocument generatedDoc : converter.convert(document)) {
                    writeConnection.addDocument(generatedDoc);
                    generatedFacetDocs++;
                }
                processedDocs++;
            }
        }
        return new FacetLoadingReport(processedDocs, generatedFacetDocs);
    }

    private void initStorage(ElasticFacetManagerConfiguration configuration) {
        final ElasticJSONStorage storage = configuration.getDestinationStorage();
        try {
            storage.deleteCollection();
        } catch (IndexMissingException ime) {
            // Pass.
        }
        final ElasticJSONStorageConnection connection = storage.openConnection();
        final Client client = connection.getClient();


        final Map<String,Object> sourceTemplate = setMappings(configuration.getProperties());

        final CreateIndexResponse response =
                client.admin().indices().prepareCreate(storage.getConfiguration().getDB())
                .setSource(sourceTemplate).execute().actionGet();

        if(!response.isAcknowledged()) throw new IllegalStateException();
    }

}
