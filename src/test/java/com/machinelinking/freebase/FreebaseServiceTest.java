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

package com.machinelinking.freebase;

import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class FreebaseServiceTest {

    @Test
    public void testEnrichment() throws IOException {
        final FreebaseService service = FreebaseService.getInstance();
        final JsonNode node = service.getEntityData("Albert Einstein");
        org.testng.Assert.assertNotNull(node);
        Assert.assertTrue(node.has("mid"));
        Assert.assertTrue(node.has("id"));
        Assert.assertTrue(node.has("name"));
    }

}
