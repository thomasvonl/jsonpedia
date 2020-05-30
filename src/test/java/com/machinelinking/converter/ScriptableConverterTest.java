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

package com.machinelinking.converter;

import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.util.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Test case for {@link com.machinelinking.converter.ScriptableConverter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ScriptableConverterTest {

    @Test
    public void testConvert() throws IOException, ConverterException, ScriptableFactoryException {
        final String script = IOUtils.toString(this.getClass().getResourceAsStream("scriptable-converter-test1.py"));
        final ScriptableConverter converter = ScriptableConverterFactory.getInstance().createConverter(script);
        final ByteArrayOutputStream serializerBAOS = new ByteArrayOutputStream();
        final JSONSerializer serializer = new JSONSerializer(serializerBAOS);
        final ByteArrayOutputStream writerBAOS = new ByteArrayOutputStream();
        final Writer writer = new BufferedWriter(new OutputStreamWriter(writerBAOS));
        converter.convertData(
            JSONUtils.parseJSONAsMap(
                "{\"@type\":\"reference\",\"label\":\"List of Nobel laureates in Physics\"," +
                 "\"content\":{\"@an0\":\"1921\"}}"
            ),
            serializer,
            writer
        );
        serializer.close();
        writer.close();
        Assert.assertEquals(
                serializerBAOS.toString(),
                "{\"link\":\"List of Nobel laureates in Physics 1921\"}"
                );
        Assert.assertEquals(
                writerBAOS.toString(),
                "<a href=\"List of Nobel laureates in Physics\">1921</a>"
        );
    }

}
