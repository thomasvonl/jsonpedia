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

package com.machinelinking.cli;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test case for {@link com.machinelinking.cli.exporter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class exporterTest {

    @Test
    public void testRun() throws IOException {
        final File in  = new File("src/test/resources/dumps/enwiki-latest-pages-articles-p1.xml.gz");
        final File out = File.createTempFile("csv-exporter", ".csv");
        final int exitCode = new exporter().run(
                String.format("--prefix http://en.wikipedia.org --in %s --out %s --threads 1",
                        in.getAbsolutePath(), out.getAbsolutePath())
                        .split(" ")
        );

        Assert.assertEquals(exitCode, 0);
        Assert.assertEquals(FileUtils.readLines(out).size(), 11761);
    }

}
