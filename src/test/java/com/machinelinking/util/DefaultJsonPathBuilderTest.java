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

package com.machinelinking.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for {@link DefaultJsonPathBuilder}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJsonPathBuilderTest {

    private JsonPathBuilder jsonPathBuilder = new DefaultJsonPathBuilder();

    @Test
    public void testPath() {
        jsonPathBuilder.enterObject();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$");
        jsonPathBuilder.field("fieldA");
        jsonPathBuilder.enterArray();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[*]");
        jsonPathBuilder.arrayElem();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[0]");
        jsonPathBuilder.arrayElem();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1]");
        jsonPathBuilder.enterObject();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1]");
        jsonPathBuilder.field("fieldB");
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1].fieldB");
        jsonPathBuilder.enterObject();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1].fieldB");
        jsonPathBuilder.field("fieldC");
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1].fieldB.fieldC");
        jsonPathBuilder.exitObject();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1].fieldB");
        jsonPathBuilder.exitObject();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA[1]");
        jsonPathBuilder.exitArray();
        Assert.assertEquals(jsonPathBuilder.getJsonPath(), "$.fieldA");
        jsonPathBuilder.exitObject();
    }

    @Test
    public void testMatch() {
        final JsonPathBuilder b1 = new DefaultJsonPathBuilder();
        b1.enterObject();
        b1.field("fi");
        b1.enterObject();
        b1.field("f2");
        b1.enterArray();
        b1.arrayElem();
        b1.arrayElem();

        final JsonPathBuilder b2 = new DefaultJsonPathBuilder();
        b2.enterObject();
        b2.field("fi");
        b2.enterObject();
        b2.field("f2");
        b2.enterArray();
        b2.arrayElem();
        b2.arrayElem();
        b2.enterObject();
        b2.field("f3");

        final JsonPathBuilder b3 = new DefaultJsonPathBuilder();
        b3.enterObject();
        b3.field("fi");
        b3.enterObject();
        b3.field("fX");
        b3.enterArray();
        b3.arrayElem();
        b3.arrayElem();
        b3.enterObject();
        b3.field("f3");

        Assert.assertTrue(
                b2.subPathOf(b1, false),
                String.format("Invalid match: %s doesn't contain %s", b2.getJsonPath(), b1.getJsonPath())
        );

        Assert.assertFalse(
                b2.subPathOf(b1, true)
        );

        Assert.assertFalse(
                b3.subPathOf(b1, true),
                String.format("Invalid match: %s should not contain %s", b1.getJsonPath(), b3.getJsonPath())
        );

    }

}
