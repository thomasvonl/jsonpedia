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

package com.machinelinking.cli;

import com.machinelinking.storage.elasticsearch.faceting.DefaultElasticFacetManagerTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test case for {@link com.machinelinking.cli.facetloader}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class facetloaderTest {

    @Test
    public void testRun() throws IOException {
        final int exitCode = new facetloader().run(
                String.format(
                        "-s localhost:%d:%s:%s -d localhost:%d:%s:%s -l 100 -c conf/faceting.properties",
                        DefaultElasticFacetManagerTest.TEST_PORT,
                        DefaultElasticFacetManagerTest.FROM_STORAGE_DB,
                        DefaultElasticFacetManagerTest.FROM_STORAGE_COLLECTION,
                        DefaultElasticFacetManagerTest.TEST_PORT,
                        DefaultElasticFacetManagerTest.FACET_TEST_DB,
                        DefaultElasticFacetManagerTest.FACET_TEST_COLLECTION
                        ).split("\\s+")
        );

        Assert.assertEquals(0, exitCode);
    }

}
