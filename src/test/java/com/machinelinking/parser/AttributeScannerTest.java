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

package com.machinelinking.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test case for {@link com.machinelinking.parser.AttributeScanner}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class AttributeScannerTest {

    @Test
    public void testScanValue() {
        final StringBuilder value = new StringBuilder();

        value.delete(0, value.length());
        AttributeScanner.scanValue("v1 post", 0, value);
        Assert.assertEquals(value.toString(), "v1");

        value.delete(0, value.length());
        AttributeScanner.scanValue("\"v1 v2\" post", 0, value);
        Assert.assertEquals(value.toString(), "v1 v2");
    }

    @Test
    public void testScan() {
        Attribute[] attributes;

        attributes = AttributeScanner.scan("v1");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[null : 'v1']");

        attributes = AttributeScanner.scan("\"v1\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[null : 'v1']");

        attributes = AttributeScanner.scan("v1 v2");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[null : 'v1', null : 'v2']");

        attributes = AttributeScanner.scan("v1 \"v2\" v3");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[null : 'v1', null : 'v2', null : 'v3']");

        /*
        attributes = AttributeScanner.scan("v1 \"v2\"  v3   \"v4\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[null : 'v1', null : 'v2', null : 'v3']");
        */

        attributes = AttributeScanner.scan("k1=v1");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : 'v1']");

        attributes = AttributeScanner.scan("k1 = v1");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : 'v1']");

        attributes = AttributeScanner.scan("k1==v1");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : '=v1']");

        attributes = AttributeScanner.scan("k1 == v1");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : '=', null : 'v1']");

        attributes = AttributeScanner.scan("k1=v1 \"v2\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : 'v1', null : 'v2']");

        attributes = AttributeScanner.scan("k1 = \"v1a v1b\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : 'v1a v1b']");

        attributes = AttributeScanner.scan("k1=v1 k2 = v2 k3 = \"v3\" k4 = \"v4a v4b\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[k1 : 'v1', k2 : 'v2', k3 : 'v3', k4 : 'v4a v4b']");
    }

    @Test
    public void testScanValueWithAssignSymbol() {
        final Attribute[] attributes = AttributeScanner.scan("name=\"Arrian 1976 loc=I, 23\"");
        Assert.assertEquals(Arrays.asList(attributes).toString(), "[name : 'Arrian 1976 loc=I, 23']");
    }

}
