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
import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageLoaderTest;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageTestBase;
import com.machinelinking.storage.elasticsearch.ElasticSelector;
import com.machinelinking.util.JSONUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

/**
 * Test case for {@link com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultElasticFacetManagerTest extends ElasticJSONStorageTestBase {

    public static final String FROM_STORAGE_DB = ElasticJSONStorageLoaderTest.TEST_STORAGE_DB;
    public static final String FROM_STORAGE_COLLECTION = ElasticJSONStorageLoaderTest.TEST_STORAGE_COLLECTION;

    public static final String FACET_TEST_DB = "jsonpedia_test_facet";
    public static final String FACET_TEST_COLLECTION = "en_section";

    @Test
    public void testSetMappings() {
        final ElasticFacetManagerConfiguration.Property[] p1 = new ElasticFacetManagerConfiguration.Property[] {
                new ElasticFacetManagerConfiguration.Property(
                        "i1",
                        "f1",
                        ElasticFacetManagerConfiguration.PropertyType.string,
                        ElasticFacetManagerConfiguration.Analyzer.not_analyzed
                )
        };
        final Map<String,?> c1 = DefaultElasticFacetManager.setMappings(p1);
        Assert.assertEquals(
                toJSONConfig(c1),
                "{\"f1_index\":{\"properties\":{\"f1\":{\"index\":\"not_analyzed\",\"type\":\"string\"}}}}"
        );

        final ElasticFacetManagerConfiguration.Property[] p2 = new ElasticFacetManagerConfiguration.Property[] {
                new ElasticFacetManagerConfiguration.Property(
                        "i1",
                        "f1",
                        ElasticFacetManagerConfiguration.PropertyType.string,
                        ElasticFacetManagerConfiguration.Analyzer.custom_lowercase
                ),
                new ElasticFacetManagerConfiguration.Property(
                        "i1",
                        "f1",
                        ElasticFacetManagerConfiguration.PropertyType.string,
                        ElasticFacetManagerConfiguration.Analyzer.custom_kstem
                )
        };
        final Map<String,?> c2 = DefaultElasticFacetManager.setMappings(p2);
        Assert.assertEquals(
                toJSONConfig(c2),
                "{\"f1_index\":" +
                "{\"properties\":" +
                "{\"f1\":{" +
                "\"type\":\"multi_field\"," +
                "\"fields\":{" +
                "\"custom_kstem\":{\"analyzer\":\"custom_kstem\",\"type\":\"string\"}," +
                "\"custom_lowercase\":{\"analyzer\":\"custom_lowercase\",\"type\":\"string\"" +
                "}}}}}}"
        );
    }

    @Test
    public void testIndexCreation() throws JSONStorageConnectionException {
        //TODO: missing explicit preconditions (population of TEST_STORAGE_DB)
        final ElasticJSONStorage fromStorage = super.createStorage(TEST_PORT, FROM_STORAGE_DB, FROM_STORAGE_COLLECTION);
        final ElasticJSONStorage facetStorage = super.createStorage(TEST_PORT, FACET_TEST_DB, FACET_TEST_COLLECTION);
        facetStorage.deleteCollection();
        final DefaultElasticFacetConfiguration configuration = new DefaultElasticFacetConfiguration(
                new File("conf/faceting.properties"),
                100,
                fromStorage,
                facetStorage
        );
        final ElasticFacetManager manager = new DefaultElasticFacetManager(configuration);
        final ElasticSelector selector = new ElasticSelector();

        final FacetLoadingReport report = manager.loadFacets(selector, new EnrichedEntityFacetConverter());

        Assert.assertEquals(report.getProcessedDocs(), 58);
        Assert.assertEquals(report.getGeneratedFacetDocs(), 1051);
    }

    private String toJSONConfig(Map<String,?> config) {
        return JSONUtils.toJsonNode((Map<String,?>) config.get("mappings")).toString();
    }

}
