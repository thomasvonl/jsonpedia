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

package com.machinelinking.storage;

import com.machinelinking.pipeline.Flag;
import com.machinelinking.pipeline.WikiPipelineFactory;
import com.machinelinking.util.FileUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Abstract loader test case.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class AbstractJSONStorageLoaderTest {

    public static final String TEST_STORAGE_DB = "jsonpedia_loader_test";
    public static final String TEST_STORAGE_COLLECTION = "en";

    private static final Flag[] FLAGS = {
            WikiPipelineFactory.Extractors,
            WikiPipelineFactory.Splitters,
            WikiPipelineFactory.Validate
    };

    private static boolean cleanupDone = false;

    protected abstract JSONStorage getJSONStorage() throws UnknownHostException;

    public void performCleanupOnce() throws UnknownHostException {
        if(!cleanupDone) {
            getJSONStorage().deleteCollection();
            Assert.assertFalse(getJSONStorage().exists(), "Collection should not exist any longer.");
            cleanupDone = true;
        }
    }

    @Test
    public void testLoaderDump1() throws IOException, SAXException {
        loadLatestPageArticles(1);
    }

    @Test
    public void testLoaderDump2() throws IOException, SAXException {
        loadLatestPageArticles(2);
    }

    @Test
    public void testLoaderDump3() throws IOException, SAXException {
        loadLatestPageArticles(3);
    }


    public void loadLatestPageArticles(int dump) throws IOException, SAXException {
        loadDump(String.format("/dumps/enwiki-latest-pages-articles-p%d.xml.gz", dump), 0);
    }

    public void loadDump(String dump, int expectedIssues) throws IOException, SAXException {
        performCleanupOnce();

        final DefaultJSONStorageLoader loader = new DefaultJSONStorageLoader(
                WikiPipelineFactory.getInstance(),
                FLAGS,
                getJSONStorage()
        );

        final StorageLoaderReport report = loader.load(
                new URL("http://en.wikipedia.org/"),
                FileUtil.openDecompressedInputStream(dump)
        );

        Assert.assertEquals(report.getPageErrors(), expectedIssues, "Unexpected number of issues.");
    }

}
