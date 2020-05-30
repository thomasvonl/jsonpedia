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

package com.machinelinking.filter;

import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test case for {@link DefaultJSONFilterEngine}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJSONFilterEngineTest {

    public static final String STRING_FILTER_EXP = "name:Death date and age,@type:template";
    public static final String REGEX_FILTER_EXP  = "url:\".*[\\s,\\d]?\\.html\",@type:link";
    public static final String NESTED_FILTER_EXP = "notable_students>@type:template,name:Plainlist>@type:reference";

    @Test
    public void testParseFilter1() throws IOException {
        final JSONFilter r = DefaultJSONFilterEngine.parseFilter(STRING_FILTER_EXP);
        Assert.assertEquals(
                r.humanReadable(),
                "object_filter(@type=template,name=Death date and age,)>null"
        );
    }

    @Test
    public void testParseFilter2() throws IOException {
        final JSONFilter r = DefaultJSONFilterEngine.parseFilter(REGEX_FILTER_EXP);
        Assert.assertEquals(
                r.humanReadable(),
                "object_filter(@type=link,url=.*[\\s,\\d]?\\.html,)>null"
        );
    }

    @Test
    public void testParseFilter3() throws IOException {
        final JSONFilter r = DefaultJSONFilterEngine.parseFilter(NESTED_FILTER_EXP);
        Assert.assertEquals(
                r.humanReadable(),
                "key_filter(notable_students)>" +
                "object_filter(@type=template,name=Plainlist,)>" +
                "object_filter(@type=reference,)>null"
        );
    }

    @Test
    public void testFilterCriteria() throws IOException {
        final JSONObjectFilter filter = new DefaultJSONObjectFilter();
        filter.addCriteria("@type", "template");
        filter.addCriteria("name"  , "Death date and age");
        checkFilter(filter, 2);
    }

    @Test
    public void testFilterCriteriaRegex() throws IOException {
        final JSONObjectFilter filter = new DefaultJSONObjectFilter();
        filter.addCriteria("@type", "template");
        filter.addCriteria("name"  , "Death .{1,4} and age");
        checkFilter(filter, 2);
    }

    @Test
    public void testFilterNestedCriteria() throws IOException {
        final JSONKeyFilter notableStudentsFilter = new DefaultJSONKeyFilter();
        notableStudentsFilter.setCriteria("notable_students");

        final JSONObjectFilter plainListFilter = new DefaultJSONObjectFilter();
        plainListFilter.addCriteria("@type", "template");
        plainListFilter.addCriteria("name", "Plainlist");

        final JSONObjectFilter typeFilter = new DefaultJSONObjectFilter();
        typeFilter.addCriteria("@type", "reference");

        notableStudentsFilter.setNested(plainListFilter);
        plainListFilter.setNested(typeFilter);

        checkFilter(notableStudentsFilter, 8); // Some duplicates because test data contain splitter replica.
    }

    @Test
    public void testFilter1() throws IOException {
        final JSONFilterFactory factory = new DefaultJSONFilterFactory();
        final JSONFilterParser parser = new DefaultJSONFilterParser();
        final JSONFilter filter = parser.parse(STRING_FILTER_EXP, factory);
        checkFilter(filter, 2);
    }

    @Test
    public void testFilter2() throws IOException {
        final JSONFilterFactory factory = new DefaultJSONFilterFactory();
        final JSONFilterParser parser = new DefaultJSONFilterParser();
        final JSONFilter filter = parser.parse(REGEX_FILTER_EXP, factory);
        checkFilter(filter, 9);
    }

    @Test
    public void testEngineApply() throws IOException {
        final JsonNode[] r = DefaultJSONFilterEngine.applyFilter(loadJSON(), STRING_FILTER_EXP);
        Assert.assertEquals(r.length, 2);
    }

    private JsonNode loadJSON() throws IOException {
        return JSONUtils.parseJSON(this.getClass().getResourceAsStream("Data.json"));
    }

    private void checkFilter(JSONFilter filter, final int EXPECTED) throws IOException {
        final JsonNode node = loadJSON();

        final JSONFilterEngine engine = new DefaultJSONFilterEngine();
        final JsonNode[] result = engine.filter(node, filter);

        Assert.assertEquals(result.length, EXPECTED);
    }

}
