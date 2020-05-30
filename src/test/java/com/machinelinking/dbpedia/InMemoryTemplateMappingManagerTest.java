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

/**
 * Test case for {@link com.machinelinking.dbpedia.InMemoryTemplateMappingManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InMemoryTemplateMappingManagerTest {

    @Test
    public void testLoadMappingFromAPI() throws TemplateMappingManagerException {
        checkMappingManager(TemplateMappingFactory.getInstance().getTemplateMappingManager("en"), 400);
        checkMappingManager(TemplateMappingFactory.getInstance().getTemplateMappingManager("de"), 280);
        checkMappingManager(TemplateMappingFactory.getInstance().getTemplateMappingManager("fr"), 180);
        checkMappingManager(TemplateMappingFactory.getInstance().getTemplateMappingManager("es"), 130);
    }

    private void checkMappingManager(TemplateMappingManager manager, int expectedMappings) {
        Assert.assertTrue(manager.getMappingsCount() > expectedMappings);
        Assert.assertEquals(manager.getMappingNames().length, manager.getMappingsCount());
        for (String name : manager.getMappingNames()) {
            Assert.assertNotNull(manager.getMapping(name));
        }
    }

}
