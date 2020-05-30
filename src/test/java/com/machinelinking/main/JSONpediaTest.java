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

package com.machinelinking.main;

import com.machinelinking.dbpedia.OntologyManager;
import com.machinelinking.dbpedia.OntologyManagerException;
import com.machinelinking.dbpedia.TemplateMapping;
import com.machinelinking.dbpedia.TemplateMappingManager;
import com.machinelinking.dbpedia.TemplateMappingManagerException;
import com.machinelinking.freebase.FreebaseService;
import com.machinelinking.storage.JSONStorageLoader;
import com.machinelinking.storage.MultiJSONStorage;
import com.machinelinking.storage.MultiJSONStorageLoaderTest;
import com.machinelinking.storage.StorageLoaderReport;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;
import com.machinelinking.storage.mongodb.MongoJSONStorage;
import com.machinelinking.util.FileUtil;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.BufferedWikiPageHandler;
import com.machinelinking.wikimedia.WikiPage;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Test case for {@link com.machinelinking.main.JSONpedia}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class JSONpediaTest {

    @Test
    public void testGetOntologyManager() throws OntologyManagerException {
        final OntologyManager ontologyManager = JSONpedia.instance().getOntologyManager();
        Assert.assertNotNull( ontologyManager.getProperty("birthDate") );
    }

    @Test
    public void testGetTemplateMappingManager() throws TemplateMappingManagerException {
        final TemplateMappingManager enTemplateMappingManager = JSONpedia.instance().getTemplateMappingManager("en");
        Assert.assertTrue( enTemplateMappingManager.getMappingNames().length > 100 );
        final TemplateMapping aMapping = enTemplateMappingManager.getMapping(enTemplateMappingManager.getMappingNames()[0]);
        Assert.assertNotNull(aMapping);
        Assert.assertNotNull(aMapping.getMappingName());
    }

    @Test
    public void testGetFreebaseService() throws TemplateMappingManagerException, IOException {
        final FreebaseService freebaseService = JSONpedia.instance().getFreebaseService();
        final JsonNode londonData = freebaseService.getEntityData("London");
        Assert.assertNotNull(londonData);
    }

    @Test
    public void testRender() throws JSONpediaException, IOException {
        final String html = JSONpedia.instance().render(
                "en:Test",
                JSONUtils.parseJSON(
                        "{\"@type\" : \"link\", \"label\" : \"Hello!\",  \"url\" : \"http://path.to/somewhere/\"}"
                )
        );
        Assert.assertNotNull(html);
    }

    @Test
    public void testRunServer() throws JSONpediaException, IOException {
        JSONpedia.instance().startServer("localhost", 9998);
        new URL("http://localhost:9998/annotate/flags").openConnection().connect();
        JSONpedia.instance().stopServer();
    }

    @Test
    public void testGetStorage() {
        final MongoJSONStorage mongoJSONStorage = (MongoJSONStorage) JSONpedia.instance()
                .getStorage(MultiJSONStorageLoaderTest.MONGO_TEST_CONN_URI);
        Assert.assertNotNull(mongoJSONStorage);

        final ElasticJSONStorage elastictJSONStorage = (ElasticJSONStorage) JSONpedia.instance()
                .getStorage(MultiJSONStorageLoaderTest.ELASTIC_TEST_CONN_URI);
        Assert.assertNotNull(elastictJSONStorage);

        final MultiJSONStorage multiJSONStorage = (MultiJSONStorage) JSONpedia.instance().getStorage(
                MultiJSONStorageLoaderTest.CONFIG_URI
        );
        Assert.assertNotNull(multiJSONStorage);
    }

    @Test
    public void testGetStorageLoader() throws IOException, SAXException {
        final String FLAGS = "Extractors";

        // Mongo Storage Loader
        final JSONStorageLoader mongoLoader =
                JSONpedia.instance().getStorageLoader(MultiJSONStorageLoaderTest.MONGO_TEST_CONN_URI, FLAGS);
        final StorageLoaderReport mongoReport = mongoLoader.load(
                new URL("http://a.wiki/prefix/1"),
                FileUtil.openDecompressedInputStream("/dumps/enwiki-latest-pages-articles-p1.xml.gz")
        );
        Assert.assertNotNull(mongoReport);
        Assert.assertEquals(mongoReport.getPageErrors(), 0);

        // Elastic Storage Loader
        final JSONStorageLoader elasticLoader =
                JSONpedia.instance().getStorageLoader(MultiJSONStorageLoaderTest.ELASTIC_TEST_CONN_URI, FLAGS);
        final StorageLoaderReport elasticReport = elasticLoader.load(
                new URL("http://a.wiki/prefix/2"),
                FileUtil.openDecompressedInputStream("/dumps/enwiki-latest-pages-articles-p1.xml.gz")
        );
        Assert.assertNotNull(elasticReport);
        Assert.assertEquals(elasticReport.getPageErrors(), 0);

        // Composed Storage Loader
        final JSONStorageLoader multiLoader = JSONpedia.instance().getStorageLoader(
                MultiJSONStorageLoaderTest.CONFIG_URI, FLAGS
        );
        final StorageLoaderReport multiReport = multiLoader.load(
                new URL("http://a.wiki/prefix/3"),
                FileUtil.openDecompressedInputStream("/dumps/enwiki-latest-pages-articles-p1.xml.gz")
        );
        Assert.assertNotNull(multiReport);
        Assert.assertEquals(multiReport.getPageErrors(), 0);
    }

    @Test
    public void testGetRawPage() throws JSONpediaException {
        final WikiPage page = JSONpedia.instance().getRawPage("en:Milan");
        Assert.assertNotNull(page);
    }

    @Test
    public void testGetRawPagesBuffer() throws IOException, JSONpediaException {
        final BufferedWikiPageHandler buffer = JSONpedia.instance().getRawPagesBuffer(
                FileUtil.openDecompressedInputStream("/dumps/enwiki-latest-pages-articles-p1.xml.gz")
        );
        WikiPage current;
        while((current = buffer.getPage(true)) != BufferedWikiPageHandler.EOQ) {
            Assert.assertNotNull(current.getContent());
        }
    }

    @Test
    public void testApplyFilter() throws JSONpediaException {
        final JsonNode london = JSONpedia.instance().process("en:London").flags("Structure").json();
        final JsonNode[] sections = JSONpedia.instance().applyFilter("@type:section", london);
        Assert.assertTrue(sections.length > 10);
    }

    @Test
    public void testProcessEntityById() throws JSONpediaException {
        final JsonNode root = JSONpedia.instance().process("en:Albert Einstein").json();
        Assert.assertEquals(root.size(), 13);
    }

    @Test
    public void testProcessEntityByURL() throws JSONpediaException {
        final JsonNode root = JSONpedia.instance().process("http://en.wikipedia.org/wiki/Albert_Einstein").json();
        Assert.assertEquals(root.size(), 13);
    }

    @Test
    public void testProcessEntityAsMap() throws JSONpediaException {
        final Map<String,?> root = JSONpedia.instance().process("en:Albert Einstein").map();
        Assert.assertEquals(root.size(), 13);
    }

    @Test
    public void testProcessEntityAsHTML() throws JSONpediaException {
        final String html = JSONpedia.instance().process("en:Albert Einstein").html();
        Assert.assertTrue(html.length() > 1000);
    }

    @Test
    public void testProcessWikiText() throws JSONpediaException {
        final JsonNode root = JSONpedia.instance()
                .process("en:Albert Einstein")
                .text("A really ''short'' description of Albert Einstein")
                .json();
        Assert.assertEquals(root.size(), 9);
    }

    @Test
    public void testProcessEntityWithFlags() throws JSONpediaException {
        final JsonNode root = JSONpedia.instance()
                .process("en:Albert Einstein").flags("Linkers,Validate,Structure").json();
        Assert.assertEquals(root.size(), 16);
    }

    @Test
    public void testProcessEntityWithFilter() throws JSONpediaException {
        final JsonNode root = JSONpedia.instance()
                .process("en:Albert Einstein").flags("Linkers,Validate,Structure").filter("@type:reference").json();
        Assert.assertEquals(root.size(), 2);
    }

}
