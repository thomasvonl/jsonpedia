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

import com.machinelinking.serializer.JSONSerializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Test case for {@link JSONUtils}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class JSONUtilsTest {

    @Test
    public void testParseJSONMulti() throws IOException {
         Assert.assertEquals(JSONUtils.parseJSONMulti("{}").length, 1);
         Assert.assertEquals(JSONUtils.parseJSONMulti("{}{}").length, 2);
         Assert.assertEquals(JSONUtils.parseJSONMulti("{}{}{}").length, 3);
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void testParseJSONMultiFail1() throws IOException {
         Assert.assertEquals(JSONUtils.parseJSONMulti("{}{").length, 1);
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void testParseJSONMultiFail2() throws IOException {
         Assert.assertEquals(JSONUtils.parseJSONMulti("{}x{}").length, 1);
    }

    @Test
    public void testJacksonNodeToSerializer() throws IOException {
        final String IN = "{ \"list\" : [\"v1\", \"2\", {\"k1\" : \"v1\"}], \"obj\" : { \"k2\" : [\"v2\"] } }";
        final JsonNode node = JSONUtils.parseJSON(IN);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JSONSerializer serializer  = new JSONSerializer(baos);
        JSONUtils.jacksonNodeToSerializer(node, serializer);
        serializer.close();
        Assert.assertEquals(JSONUtils.parseJSON(baos.toString()), node);
    }

    @Test
    public void testJSONMapArrayToSerializer() throws IOException {
        checkJSONMapArrayToSerializer(
                new Object[]{1, 2, "x"},
                "[1,2,\"x\"]"
        );
        checkJSONMapArrayToSerializer(
                new HashMap<String,Object>(){{ put("k1", new Object[]{1, 2, "x"}); }},
                "{\"k1\":[1,2,\"x\"]}"
        );
    }

    private void checkJSONMapArrayToSerializer(Object in, String expected) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(baos);
        JSONUtils.jsonMapArrayToSerializer(in, serializer);
        serializer.close();
        Assert.assertEquals(baos.toString(), expected);
    }

}
