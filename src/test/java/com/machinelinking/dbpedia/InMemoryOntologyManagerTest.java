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

package com.machinelinking.dbpedia;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Test case for {@link InMemoryOntologyManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InMemoryOntologyManagerTest {

    private static final int ESPECTED_PROPS_COUNT = 3400;

    @Test
    public void testInitialization() throws OntologyManagerException {
        Map<String,Property> propertyMappings = InMemoryOntologyManager.initOntologyIndex(false);
        Assert.assertTrue(propertyMappings.size() > ESPECTED_PROPS_COUNT);
        for(Map.Entry<String,Property> mapping : propertyMappings.entrySet()) {
            Assert.assertNotNull(mapping.getValue().getPropertyName());
            Assert.assertNotNull(mapping.getValue().getPropertyLabel());
        }
    }

    @Test
    public void testGetPropertiesCount() throws OntologyManagerException {
        final InMemoryOntologyManager manager = new InMemoryOntologyManager();
        Assert.assertTrue(manager.getPropertiesCount() > ESPECTED_PROPS_COUNT);
    }

    @Test
    public void testGetPropertyNames() throws OntologyManagerException {
        final InMemoryOntologyManager manager = new InMemoryOntologyManager();
        Assert.assertEquals(manager.getPropertiesCount(), manager.getPropertyNames().size());
    }

    @Test
    public void testGetMapping() throws OntologyManagerException {
        final InMemoryOntologyManager manager = new InMemoryOntologyManager();
        Assert.assertEquals(
                manager.getProperty("birthDate").toString(),
                "{property=birthDate label='birth date' domain=Person range=xsd:date}"
        );
        Assert.assertEquals(
                manager.getProperty("birthPlace").toString(),
                "{property=birthPlace label='birth place' domain=Person range=Place}"
        );
        Assert.assertEquals(
                manager.getProperty("spouse").toString(),
                "{property=spouse label='spouse' domain=Person range=Person}"
        );
        Assert.assertEquals(
                manager.getProperty("successor").toString(),
                "{property=successor label='successor' domain=null range=null}"
        );
    }

}
