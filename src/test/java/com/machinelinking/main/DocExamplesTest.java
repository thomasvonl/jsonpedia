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

import com.machinelinking.converter.Converter;
import com.machinelinking.converter.ConverterException;
import com.machinelinking.converter.ConverterManager;
import com.machinelinking.converter.DefaultConverterManager;
import com.machinelinking.converter.ScriptableConverter;
import com.machinelinking.converter.ScriptableConverterFactory;
import com.machinelinking.converter.ScriptableFactoryException;
import com.machinelinking.filter.DefaultJSONFilterEngine;
import com.machinelinking.filter.JSONFilter;
import com.machinelinking.filter.JSONFilterEngine;
import com.machinelinking.filter.JSONObjectFilter;
import com.machinelinking.pagestruct.WikiTextHRDumperHandler;
import com.machinelinking.parser.DocumentSource;
import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.serializer.Serializer;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

/**
 * This test case verifies the code snippets used in documentation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DocExamplesTest {

    @Test
    public void test2MinutesTutorialCodeSnippet() throws JSONpediaException {
        JSONpedia jsonpedia = JSONpedia.instance();
        JsonNode root = jsonpedia.process("de:Liste_hundertjähriger_Persönlichkeiten").flags("Structure").json();
        System.out.println(root);
        JsonNode[] sections = jsonpedia.applyFilter("@type:section", root);
        String firstSectionHTML = jsonpedia.render("en:London", sections[0]);
        Assert.assertNotNull(firstSectionHTML);
        JsonNode[] allReferencesInSections = jsonpedia.applyFilter("@type:section>@type:reference", root);
        String allReferencesHTML = jsonpedia.render("en:London", allReferencesInSections);
        Assert.assertNotNull(allReferencesHTML);
    }

    @Test
    public void testParseSnippet() throws IOException, WikiTextParserException {
        final WikiTextHRDumperHandler handler = new WikiTextHRDumperHandler();
        WikiTextParser parser = new WikiTextParser(handler);
        parser.parse(
                new DocumentSource(new URL("http://test.com/url"), "This is a ''WikiText'' example")
        );

        Assert.assertEquals(
                handler.getContent(),

                "Begin Document\n" +
                "Text: 'This is a '\n" +
                "ItalicBold: 2\n" +
                "Text: 'WikiText'\n" +
                "ItalicBold: 2\n" +
                "Text: ' example'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseFilterAndApply() throws JSONpediaException {
        final JSONFilter filter = DefaultJSONFilterEngine.parseFilter("@type:section>@type:reference");
        Assert.assertEquals(filter.humanReadable(), "object_filter(@type=section,)>object_filter(@type=reference,)>null");
        JSONFilterEngine engine = new DefaultJSONFilterEngine();
        final JsonNode london = JSONpedia.instance().process("en:London").flags("Structure").json();
        JsonNode[] result = engine.filter(london, filter);
        Assert.assertTrue(result.length > 10);
    }

    @Test
    public void testConverterSnippet() throws IOException, ConverterException, JSONpediaException {
        ConverterManager converterManager = new DefaultConverterManager();
        final Converter converter = new Converter() { // Upper case content writer.
            @Override
            public void convertData(Map<String, ?> data, Serializer serializer, Writer writer)
                throws ConverterException {
                try {
                    writer.append( data.get("content").toString().toUpperCase() );
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        };
        converterManager.addConverter(
                (JSONObjectFilter) DefaultJSONFilterEngine.parseFilter("@type:reference"),
                converter
        );

        final ByteArrayOutputStream serializerBuffer = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(serializerBuffer);
        final ByteArrayOutputStream writerBuffer = new ByteArrayOutputStream();
        final Writer writer = new PrintWriter(writerBuffer);
        final JsonNode data = JSONpedia.instance().process("en:London").flags("Structure").json();
        converterManager.process(data, serializer, writer);
        serializer.close();
        writer.close();

        Assert.assertTrue(serializerBuffer.size() == 0);
        Assert.assertTrue(writerBuffer.toString().contains("LONDON"));
    }

    @Test
    public void testScriptableConverterSnippet()
    throws ScriptableFactoryException, IOException, JSONpediaException, ConverterException {
        final String script =
                "def convert_data(d):\n" +
                "    return {'link' : '%s %s' % (d['label'], functions.as_text(d['content'])) }\n" +
                "\n" +
                "def convert_hr(d):\n" +
                "    return '<a href=\"%s\">%s</a>' % (d['label'], functions.as_text(d['content']))";

        final ScriptableConverter converter = ScriptableConverterFactory.getInstance().createConverter(script);
        final ConverterManager converterManager = new DefaultConverterManager();
        converterManager.addConverter(
                (JSONObjectFilter) DefaultJSONFilterEngine.parseFilter("@type:reference"),
                converter
        );

        final ByteArrayOutputStream serializerBuffer = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(serializerBuffer);
        final ByteArrayOutputStream writerBuffer = new ByteArrayOutputStream();
        final Writer writer = new BufferedWriter(new OutputStreamWriter(writerBuffer));
        final JsonNode data = JSONpedia.instance().process("en:London").flags("Structure").json();
        converterManager.process(data, serializer, writer);
        serializer.close();
        writer.close();

        Assert.assertTrue(serializerBuffer.toString().length() > 0);
        Assert.assertTrue(writerBuffer.toString().length() > 0);
    }

}
