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

package com.machinelinking.serializer;

import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test case for {@link JSONSerializer}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class JSONSerializerTest {

    private ByteArrayOutputStream baos;
    private Serializer serializer;

    @BeforeMethod
    public void setUp() throws IOException {
        baos = new ByteArrayOutputStream();
        serializer = new JSONSerializer(baos);
    }

    @AfterMethod
    public void tearDown() {
        baos = null;
        serializer = null;
    }

    @Test
    public void testOpenObject() {
        serializer.openObject();
        verify("{}");
    }

    @Test
    public void testOpenObject2() {
        serializer.openObject();
        serializer.openObject();
        verify("{ \"@an0\" : {} }");
    }

    @Test
    public void testOpenObject3() {
        serializer.openList();
        serializer.openObject();
        verify("[{}]");
    }

    @Test
    public void testOpenObject4() {
        serializer.openList();
        serializer.field("f1");
        serializer.openObject();
        verify("[ { \"f1\" : null } , {} ]");
    }

    @Test
    public void testOpenList() {
        serializer.openList();
        verify("null");
    }

    @Test
    public void testOpenList2() {
        serializer.openList();
        serializer.openList();
        verify("[null]");
    }

    @Test
    public void testSpuriousField() {
        serializer.openList();
        serializer.field("f1");
        verify("[{ \"f1\" : null }]");
    }

    @Test
    public void testSpuriousField2() {
        serializer.openList();
        serializer.field("f1");
        serializer.field("f2");
        verify("[ { \"f1\" :  null } , { \"f2\" : null } ]");
    }

    @Test
    public void testSpuriousField3() {
        serializer.openList();
        serializer.field("f1");
        serializer.field("f2");
        serializer.value("v1");
        verify("[{ \"f1\" : null } , { \"f2\" : \"v1\" } ]");
    }

    @Test
    public void testSpuriousValue() {
        serializer.openObject();
        serializer.value("v1");
        verify("{ \"@an0\" : \"v1\" }");
    }

    @Test
    public void testSpuriousValue2() {
        serializer.openObject();
        serializer.value("v1");
        serializer.value("v2");
        verify("{ \"@an0\" : \"v1\" , \"@an1\" : \"v2\" }");
    }

    @Test
    public void testPendingObject() {
        serializer.openObject();
        serializer.field("f1");
        serializer.value("v1");
        serializer.field("f2");
        serializer.closeObject();
        verify("{ \"f1\" : \"v1\" , \"f2\" : null }");
    }

    @Test
    public void testObjectSequence() {
        serializer.openObject();
        serializer.field("f1");
        serializer.value("v1");
        serializer.field("f2");
        serializer.value("v2");
        serializer.closeObject();
        verify("{ \"f1\" : \"v1\" , \"f2\" : \"v2\" }");
    }

    @Test
    public void testListSequence() {
        serializer.openList();
        serializer.value("v1");
        serializer.value("v2");
        serializer.value("v3");
        serializer.closeList();
        verify("[ \"v1\" , \"v2\" , \"v3\" ]");
    }

    @Test
    public void testNestedSequence1() {
        serializer.openObject();
        serializer.field("f1");
        serializer.openObject();
        verify("{ \"f1\" : {} }");
    }

    @Test
    public void testNestedSequence2() {
        serializer.openObject();
        serializer.field("f1");
        serializer.openList();
        verify("{ \"f1\" : null }");
    }

    @Test
    public void testNestedSequence3() {
        serializer.openList();
        serializer.field("f1");
        serializer.openList();
        verify("[ { \"f1\" : null }, null ]");
    }

    private void verify(String expected) {
        serializer.close();
        final String out = baos.toString();
        try {
            final JsonNode expectedNode = JSONUtils.parseJSON(expected);
            final JsonNode outNode = JSONUtils.parseJSON(out);
            Assert.assertEquals(outNode, expectedNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
