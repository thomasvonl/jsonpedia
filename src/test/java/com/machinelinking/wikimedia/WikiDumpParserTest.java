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
import java.util.zip.GZIPInputStream;

/**
 * Test case for {@link WikiDumpParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiDumpParserTest {

    @Test
    public void testParseDump1() throws IOException, SAXException {
        parseAndVerifyDump("/dumps/enwiki-latest-pages-articles-p1.xml.gz", 10);
    }

    @Test
    public void testParseDump2() throws IOException, SAXException {
        parseAndVerifyDump("/dumps/enwiki-latest-pages-articles-p2.xml.gz", 23);
    }

    @Test
    public void testParseDump3() throws IOException, SAXException {
        parseAndVerifyDump("/dumps/enwiki-latest-pages-articles-p3.xml.gz", 25);
    }

    private void parseAndVerifyDump(String dump, int expected) throws IOException, SAXException {
        final WikiDumpParser dumpParser = new WikiDumpParser();
        final BufferedWikiPageHandler bufferedHandler = new BufferedWikiPageHandler();
        dumpParser.parse(
                bufferedHandler,
                new GZIPInputStream( this.getClass().getResourceAsStream(dump) )
        );

        int count = 0;
        WikiPage page;
        while( (page = bufferedHandler.getPage(false)) != BufferedWikiPageHandler.EOQ) {
            Assert.assertTrue(page.getId() > 1);
            Assert.assertTrue(page.getTitle().trim().length() > 0);
            Assert.assertTrue(page.getContent().trim().length() > 0);
            count++;
        }
        Assert.assertEquals(count, expected);
    }

}
