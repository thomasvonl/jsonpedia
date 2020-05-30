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

package com.machinelinking.storage.mongodb;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for {@link com.machinelinking.storage.mongodb.MongoSelector}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoSelectorParserTest {

    @Test
    public void testParser() {
        final MongoSelectorParser parser = MongoSelectorParser.getInstance();
        Assert.assertEquals(
                parser.parse("name = Albert Einstein -> content.categories").toString(),
                "criterias: [name eq 'Albert Einstein'], projections: [content, _id, name, content.categories, version]"

        );

        final MongoSelector selector = parser.parse(
                "version <= #0, _id > #10, content.categories.content = Cosmologists, content.sections.title = Biography " +
                "-> _id, name, content"
        );
        Assert.assertEquals(
                selector.toString(),
                "criterias: [version lte 0, _id gt 10, content.categories.content eq 'Cosmologists', content.sections.title eq 'Biography'], " +
                "projections: [content, _id, name, version]"
        );

        Assert.assertEquals(
                parser.parse("_id > #1 -> _id").toString(),
                "criterias: [_id gt 1], projections: [content, _id, name, version]"
        );
    }

}
