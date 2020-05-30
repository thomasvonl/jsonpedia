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

import com.machinelinking.storage.SelectorParserException;
import com.machinelinking.storage.elasticsearch.ElasticSelectorParser;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for {@link com.machinelinking.storage.elasticsearch.ElasticSelectorParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticSelectorParserTest {

    @Test
    public void testParse() throws SelectorParserException {
        Assert.assertEquals(ElasticSelectorParser.getInstance().parse("value").toString(), "_any eq 'value'");
        Assert.assertEquals(ElasticSelectorParser.getInstance().parse("name:value").toString(), "name eq 'value'");
        Assert.assertEquals(ElasticSelectorParser.getInstance().parse("name:\"value\"").toString(), "name eq 'value'");
        Assert.assertEquals(ElasticSelectorParser.getInstance().parse("name:\"v1 AND v2\"").toString(), "name eq 'v1 AND v2'");
        Assert.assertEquals(ElasticSelectorParser.getInstance().parse("name:\"v1 OR v2\"").toString(), "name eq 'v1 OR v2'");
    }

}
