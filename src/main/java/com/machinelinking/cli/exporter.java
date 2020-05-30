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

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.machinelinking.exporter.CSVExporter;
import com.machinelinking.exporter.CSVExporterReport;
import com.machinelinking.exporter.TemplatePropertyCSVExporter;
import com.machinelinking.exporter.TextCSVExporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Commandline utility for {@link com.machinelinking.exporter.CSVExporter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class exporter {

    public static final String FORMAT_TEMPLATE_PROP = "templateprop";
    public static final String FORMAT_TEXT = "text";
    public static final String[] FORMATS = {FORMAT_TEMPLATE_PROP, FORMAT_TEXT};

    @Parameter(
            names = {"--prefix", "-p"},
            description = "Page prefix",
            converter = CLIUtils.URLConverter.class,
            required = true
    )
    private URL prefix;

    @Parameter(
            names = {"--in", "-i"},
            description = "Input file",
            converter = CLIUtils.FileConverter.class,
            required = true,
            validateValueWith = CLIUtils.ExistingFile.class
    )
    private File in;

    @Parameter(
            names = {"--out", "-o"},
            description = "Output file",
            required = true,
            converter = CLIUtils.FileConverter.class
    )
    private File out;

    @Parameter(
            names = {"--format", "-f"},
            description = "Format",
            required = false,
            validateValueWith=FormatValidator.class
    )
    private String format = FORMAT_TEMPLATE_PROP;

    @Parameter(
            names = {"--threads", "-t"},
            description = "Number of threads",
            validateValueWith = CLIUtils.NumOfThreadsValidator.class
    )
    private int numOfThreads = 0; // Num of threads based on CPU cores.

    public exporter() {
    }

    public static void main(String[] args) {
        exporter cli = new exporter();
        System.exit( cli.run(args) );
    }

    public int run(String[] args) {
        final JCommander commander = new JCommander(this);
        int exitCode;
        try {
            commander.parse(args);
            final CSVExporter exporter = createExporter(format);
            exporter.setThreads(numOfThreads);
            final CSVExporterReport report = exporter.export(prefix, in, out);
            System.out.println(report);
            exitCode = 0;
        } catch (ParameterException pe) {
            System.err.println(pe.getMessage());
            commander.usage();
            exitCode = 1;
        } catch (FileNotFoundException fnfe) {
            System.err.println("Cannot find input file.");
            exitCode = 2;
        } catch (Exception e) {
            System.err.println("Error while exporting CSV.");
            e.printStackTrace();
            exitCode = 3;
        }
        return exitCode;
    }

    private CSVExporter createExporter(String exporter) {
        switch (exporter) {
            case FORMAT_TEMPLATE_PROP:
                return new TemplatePropertyCSVExporter();
            case FORMAT_TEXT:
                return new TextCSVExporter();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static class FormatValidator implements IValueValidator<String> {
        @Override
        public void validate(String name, String value) throws ParameterException {
            for(String format: exporter.FORMATS) {
                if(format.equals(value)) return;
            }
            throw new ParameterException("Invalid format: " + value);
        }
    }

}
