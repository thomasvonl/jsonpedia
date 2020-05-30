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

package com.machinelinking.exporter;

import com.machinelinking.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 *  Test case for {@link TemplatePropertyCSVExporter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TemplatePropertyCSVExporterTest {

    @Test
    public void testExport() throws IOException {
        final TemplatePropertyCSVExporter exporter = new TemplatePropertyCSVExporter();
        exporter.setThreads(1);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CSVExporterReport report = exporter.export(
                new URL("http://en.wikipedia.org/"),
                FileUtil.openDecompressedInputStream("/dumps/enwiki-latest-pages-articles-p1.xml.gz"),
                out
        );

        Assert.assertEquals(report.getTemplatesCount(), 1133);
        final String expected = IOUtils.toString( this.getClass().getResourceAsStream("template-prop-out.csv") );
        Assert.assertEquals(out.toString(), expected);
    }

}
