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
 * Test case for {@link com.machinelinking.util.StringUtils}
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class StringUtilsTest {

    @Test
    public void testStripTags() {
        Assert.assertEquals(StringUtils.stripTags("a b c"), "a b c");
        Assert.assertEquals(StringUtils.stripTags("a <b> c"), "a  c");
        Assert.assertEquals(StringUtils.stripTags("a <b"), "a ");
        Assert.assertEquals(StringUtils.stripTags("a <b x x x> c <d y y y> e"), "a  c  e");
        Assert.assertEquals(StringUtils.stripTags("a <b x x<> x> c <d y y< y> e"), "a  c ");
    }

}
