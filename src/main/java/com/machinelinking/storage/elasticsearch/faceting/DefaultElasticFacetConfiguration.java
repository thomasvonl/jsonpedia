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

import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of {@link ElasticFacetManagerConfiguration}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultElasticFacetConfiguration implements ElasticFacetManagerConfiguration {

    public static final String MAPPING_PROPERTY_PREFIX = "mapping.";

    private int limit;
    private ElasticJSONStorage inStorage;
    private ElasticJSONStorage outStorage;

    private final List<Property> properties = new ArrayList<>();

    public DefaultElasticFacetConfiguration(
            Properties facetConfig, int limit, ElasticJSONStorage inStorage, ElasticJSONStorage outStorage
    ) {
        init(facetConfig, limit, inStorage, outStorage);
    }

    public DefaultElasticFacetConfiguration(
            File facetConfigFile, int limit, ElasticJSONStorage inStorage, ElasticJSONStorage outStorage
    ) {
        final Properties faceConfig = new Properties();
        try {
            try(FileInputStream fis = new FileInputStream(facetConfigFile)) {
                faceConfig.load(fis);
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Cannot find or open file " + facetConfigFile, ioe);
        }
        init(faceConfig, limit, inStorage, outStorage);
    }

    private void init(Properties facetConfig, int limit, ElasticJSONStorage inStorage, ElasticJSONStorage outStorage) {
        this.limit = limit;
        this.inStorage = inStorage;
        this.outStorage = outStorage;
        loadProperties(facetConfig);
    }

    public void addProperty(String indexName, String field, PropertyType type, Analyzer analyzer) {
        properties.add( new Property(indexName, field, type, analyzer) );
    }

    public boolean removeProperty(String indexName, String field) {
        List<Property> targets = new ArrayList<>();
        for(Property property : properties) {
            if(property.indexName.equals(indexName)) {
                targets.add(property);
            }
        }
        if(!properties.isEmpty())
            return properties.removeAll(targets);
        return false;
    }

    @Override
    public Property[] getProperties() {
        return properties.toArray(new Property[0]);
    }

    @Override
    public ElasticJSONStorage getSourceStorage() {
        return inStorage;
    }

    @Override
    public ElasticJSONStorage getDestinationStorage() {
        return outStorage;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    private void loadPropertyValue(String name, String value) {
        String[] mapping = value.split(":");
        final boolean missingAnalyzer = mapping.length == 2;
        if (!missingAnalyzer && mapping.length != 3)
            throw new IllegalArgumentException(String.format("Invalid mapping definition for line [%s]", value));
        addProperty(
                name,
                mapping[0],
                PropertyType.valueOf(mapping[1]),
                missingAnalyzer ? Analyzer.not_analyzed : Analyzer.valueOf(mapping[2])
        );
    }

    private void loadProperties(Properties props) {
        String key, value, name;
        String[] declarations;
        for(Map.Entry<Object,Object> prop : props.entrySet()) {
            key = prop.getKey().toString();
            value = prop.getValue().toString();
            if(key.startsWith(MAPPING_PROPERTY_PREFIX)) {
                name = key.substring(MAPPING_PROPERTY_PREFIX.length());
                declarations = value.split("\\|");
                for(String declaration : declarations) {
                    loadPropertyValue(name, declaration);
                }
            }
        }
    }
}
