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

import com.machinelinking.parser.DefaultWikiTextParserHandler;
import com.machinelinking.parser.DocumentSource;
import com.machinelinking.parser.WikiTextParser;
import com.machinelinking.util.FileUtil;
import com.machinelinking.wikimedia.PageProcessor;
import com.machinelinking.wikimedia.ProcessorReport;
import com.machinelinking.wikimedia.WikiDumpMultiThreadProcessor;
import com.machinelinking.wikimedia.WikiPage;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Extracts raw text content from a Wikimedia dump.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TextCSVExporter
extends WikiDumpMultiThreadProcessor<TextCSVExporter.TextProcessor>
implements CSVExporter {

    private static final Logger logger = Logger.getLogger(TextCSVExporter.class);

    private PrintWriter writer;
    private int threads = 1;

    private String pageTitle;

    @Override
    public void setThreads(int num) {
        if(num < 0) throw new IllegalArgumentException("Invalid thread size:" + num);
        threads = num;
    }

    @Override
    public CSVExporterReport export(URL pagePrefix, InputStream is, OutputStream os) {
        final BufferedInputStream bis =
                is instanceof BufferedInputStream ? (BufferedInputStream) is : new BufferedInputStream(is);
        writer = new PrintWriter( new OutputStreamWriter(os) );
        try {
            final ProcessorReport report = super.process(
                    pagePrefix,
                    bis,
                    threads <= 0 ? super.getBestNumberOfThreads() : threads
            );
            return new CSVExporterReport(report, 0, 0, 0); //TODO
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CSVExporterReport export(URL pagePrefix, File in, File out) throws IOException {
        return export(pagePrefix, FileUtil.openDecompressedInputStream(in), new FileOutputStream(out));
    }

    @Override
    public void initProcess() {
         // Empty.
    }

    @Override
    public TextProcessor initProcessor(int threadNumber) {
        return new TextProcessor();
    }

    @Override
    public void finalizeProcessor(TextProcessor processor) {
        // Empty.
    }

    @Override
    public void finalizeProcess(ProcessorReport report) {
        writer.close();
    }

    private void printOpenPage(String pageId) {
        printText(String.format("<%s>", pageId));
    }

    private void printClosePage(String pageId) {
        printText(String.format("</%s>\n", pageId));
    }

    private void printText(String txt) {
        writer.write(txt);
        writer.print(' ');
    }

    class TextProcessor extends DefaultWikiTextParserHandler implements PageProcessor {

        private final WikiTextParser parser = new WikiTextParser(this);

        private long processedPages;
        private long errorPages;

        private int insideStructure = 0;

        @Override
        public void processPage(String pagePrefix, String threadId, WikiPage page) {
            pageTitle = page.getTitle();
            try {
                parser.parse(new DocumentSource(new URL(pagePrefix), page.getContent()));
            } catch (Exception e) {
                errorPages++;
                logger.error("Error while parsing page " + page.getTitle(), e);
            } finally {
                processedPages++;
            }
        }

        @Override
        public long getProcessedPages() {
            return processedPages;
        }

        @Override
        public long getErrorPages() {
            return errorPages;
        }

        @Override
        public void beginTemplate(TemplateName name) {
            insideStructure++;
        }

        @Override
        public void endTemplate(TemplateName name) {
            insideStructure--;
        }

        @Override
        public void beginDocument(URL document) {
            printOpenPage(pageTitle);
        }

        @Override
        public void endDocument() {
            printClosePage(pageTitle);
        }

        @Override
        public void text(String content) {
            if(insideStructure == 0)
                printText(content);
        }
    }

}
