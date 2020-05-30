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
 * {@link Runnable} implementation for {@link PageProcessor}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiDumpRunnable implements Runnable {

    private final String threadId;
    private final String pagePrefix;
    private final BufferedWikiPageHandler bufferedHandler;
    private final PageProcessor processor;

    public WikiDumpRunnable(
            String threadId,
            String pagePrefix,
            BufferedWikiPageHandler bufferedHandler,
            PageProcessor processor
    ) {
        this.threadId = threadId;
        this.pagePrefix = pagePrefix;
        this.bufferedHandler = bufferedHandler;
        this.processor = processor;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getPagePrefix() {
        return pagePrefix;
    }

    @Override
    public void run() {
        WikiPage page;
        while ((page = bufferedHandler.getPage(true)) != BufferedWikiPageHandler.EOQ) {
            processor.processPage(pagePrefix, threadId, page);
        }
    }

}
