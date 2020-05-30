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

/**
 * The report of a {@link PageProcessor}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ProcessorReport {

    private final long processedPages;
    private final long pagesWithError;
    private final long elapsedTime;

    public ProcessorReport(
            long processedPages, long pagesWithError,
            long elapsedTime
    ) {
        this.processedPages = processedPages;
        this.pagesWithError = pagesWithError;
        this.elapsedTime = elapsedTime;
    }

    public long getProcessedPages() {
        return processedPages;
    }

    public long getPagesWithError() { return pagesWithError; }

    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public String toString() {
        return String.format(
                "Processed pages: %d, pages with errors: %d, elapsed time: %ds (%dms)",
                processedPages,
                pagesWithError,
                elapsedTime / 1000,
                elapsedTime
        );
    }

}
