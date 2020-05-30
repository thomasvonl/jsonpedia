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

package com.machinelinking.wikimedia;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * Test case for {@link WikiAPIParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiAPIParserTest {

    @Test
    public void testParseAPIResponse() throws IOException, SAXException {
        final WikiPage page = WikiAPIParser.parseAPIResponse(
                WikimediaUtils.entityToWikiTextURLAPI( new URL("http://en.wikipedia.org/Albert_Einstein") )
        );
        Assert.assertNotNull(page);
        Assert.assertEquals(page.getTitle(), "Albert Einstein");
        Assert.assertEquals(page.getId(), 736);
        Assert.assertTrue(page.getContent().trim().length() > 0);
    }

}
