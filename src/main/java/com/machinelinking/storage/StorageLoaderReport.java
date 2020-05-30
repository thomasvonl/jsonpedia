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

/**
 * Report produced by a {@link JSONStorageLoader}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class StorageLoaderReport {

    private long processedPages;
    private long pageErrors;
    private long elapsedTime;

    public StorageLoaderReport(long processedPages, long pageErrors, long elapsedTime) {
        this.processedPages = processedPages;
        this.pageErrors = pageErrors;
        this.elapsedTime = elapsedTime;
    }

    public long getProcessedPages() {
        return processedPages;
    }

    public long getPageErrors() {
        return pageErrors;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public String toString() {
        return String.format(
                "Processed pages: %d, pages with error: %d, elapsed time: %d ms, pages/ms: %f, errors/total pages: %f",
                processedPages, pageErrors, elapsedTime,
                (processedPages / (float)elapsedTime),
                (pageErrors / (float) processedPages)
        );
    }

}
