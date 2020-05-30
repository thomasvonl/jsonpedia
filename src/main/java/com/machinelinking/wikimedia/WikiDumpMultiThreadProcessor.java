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

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A multi-thread executor for {@link PageProcessor}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class WikiDumpMultiThreadProcessor <P extends PageProcessor> {

    public static final int MIN_NUM_OF_THREADS = 2;

    private static final Logger logger = Logger.getLogger(WikiDumpMultiThreadProcessor.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean stopAtFirstError = true;

    private URL pagePrefix;

    private long startTime;
    private long totalProcessedPages = 0;
    private long totalErrorPages = 0;

    public WikiDumpMultiThreadProcessor() {
    }

    public abstract void initProcess();

    public abstract P initProcessor(int threadNumber);

    public abstract void finalizeProcessor(P processor);

    public abstract void finalizeProcess(ProcessorReport report);

    public URL getPagePrefix() {
        return pagePrefix;
    }

    public boolean isStopAtFirstError() {
        return stopAtFirstError;
    }

    public void setStopAtFirstError(boolean stopAtFirstError) {
        this.stopAtFirstError = stopAtFirstError;
    }

    public ProcessorReport process(URL pagePrefix, InputStream is) throws IOException, SAXException {
        return process(pagePrefix, is, getBestNumberOfThreads());
    }

    public ProcessorReport process(URL pagePrefix, final InputStream is, int threads) throws IOException, SAXException {
        if(pagePrefix == null) throw new NullPointerException();
        if (threads <= 0) throw new IllegalArgumentException("Invalid number of threads: " + threads);
        this.pagePrefix = pagePrefix;
        this.startTime = System.currentTimeMillis();
        this.totalErrorPages = this.totalProcessedPages = 0;

        logger.info("Starting processing with " + threads + " threads");

        initProcess();

        final WikiDumpParser dumpParser = new WikiDumpParser();
        final BufferedWikiPageHandler bufferedHandler = new BufferedWikiPageHandler();

        final List<Future> futures = new ArrayList<>();
        final List<RunnableProcessor> runnableProcessors = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final P processor = initProcessor(t);
            final WikiDumpRunnable runnable = new WikiDumpRunnable(
                    "thread" + t,
                    pagePrefix.toExternalForm(),
                    bufferedHandler,
                    processor
            );
            final Future future = executorService.submit(runnable);
            runnableProcessors.add( new RunnableProcessor(runnable, processor) );
            futures.add(future);
        }

        final Thread parserThread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    dumpParser.parse(bufferedHandler, is);
                } catch (Exception e) {
                    throw new RuntimeException("Error while processing parse stream.", e);
                }
            }
        });
        try {
            parserThread.start();
            parserThread.join();
        } catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted parser thread.", ie);
        }

        final ProcessorReport report;
        try {
            for (Future future : futures) {
                try {
                    future.get(20, TimeUnit.SECONDS); //TODO: this must be not necessary. Investigate.
                } catch (ExecutionException ee) {
                    if(stopAtFirstError) {
                        throw new RuntimeException("Error while executing thread.", ee);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error while waiting operation completion.", e);
        } finally {
            logger.info("Process closed.");
            for (RunnableProcessor runnableProcessor : runnableProcessors) {
                totalProcessedPages += runnableProcessor.processor.getProcessedPages();
                totalErrorPages += runnableProcessor.processor.getErrorPages();
                finalizeProcessor(runnableProcessor.processor);
            }
            report = createReport();
            finalizeProcess(report);
        }
        return report;
    }

    public ProcessorReport createReport() {
        final long elapsedTime = System.currentTimeMillis() - startTime;
        return new ProcessorReport(
                totalProcessedPages,
                totalErrorPages,
                elapsedTime
        );
    }

    protected int getBestNumberOfThreads() {
        final int candidate = Runtime.getRuntime().availableProcessors();
        return candidate < MIN_NUM_OF_THREADS ? MIN_NUM_OF_THREADS : candidate;
    }

    class RunnableProcessor {
        final WikiDumpRunnable runnable;
        final P processor;

        RunnableProcessor(WikiDumpRunnable runnable, P processor) {
            this.runnable = runnable;
            this.processor = processor;
        }
    }

}
