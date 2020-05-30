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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Test case for {@link com.machinelinking.wikimedia.BufferedWikiPageHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class BufferedWikiPageHandlerTest {

    @Test
    public void testWriteOneConsumeMulti() throws InterruptedException {
        for(int i = 0; i < 10; i++) {
            writeOneConsumeMulti();
        }
    }

    private void writeOneConsumeMulti() throws InterruptedException {
        final BufferedWikiPageHandler wikiPageHandler = new BufferedWikiPageHandler();
        final int COUNT = 2000;
        final Thread wThread = new Thread( new PageWriter(COUNT, wikiPageHandler) );
        final PageReader r1 = new PageReader(wikiPageHandler);
        final PageReader r2 = new PageReader(wikiPageHandler);
        final PageReader r3 = new PageReader(wikiPageHandler);
        final PageReader r4 = new PageReader(wikiPageHandler);
        final Thread r1Thread = new Thread(r1);
        final Thread r2Thread = new Thread(r2);
        final Thread r3Thread = new Thread(r3);
        final Thread r4Thread = new Thread(r4);
        r1Thread.start();
        r2Thread.start();
        r3Thread.start();
        r4Thread.start();
        wThread.start();
        wThread.join();
        r1Thread.join();
        r2Thread.join();
        r3Thread.join();
        r4Thread.join();
        Assert.assertEquals(r1.getCounter() + r2.getCounter() + r3.getCounter() + r4.getCounter(), COUNT);
    }

    class PageWriter implements Runnable {

        private final int total;
        private final BufferedWikiPageHandler handler;

        PageWriter(int count, BufferedWikiPageHandler handler) {
            this.total = count;
            this.handler = handler;
        }

        @Override
        public void run() {
            this.handler.startStream();
            int pageId = 0;
            for(int i = 0; i < total; i++) {
                if(Thread.interrupted()) break;
                this.handler.startWikiPage(pageId, 1, "page " + pageId);
                this.handler.wikiPageContent(new char[0], 0, 0);
                this.handler.endWikiPage();
                pageId++;
            }
            this.handler.endStream();
        }

    }

    class PageReader implements Runnable {

        private final BufferedWikiPageHandler handler;
        private final Random random = new Random();

        private int counter;

        PageReader(BufferedWikiPageHandler handler) {
            this.handler = handler;
        }

        public int getCounter() {
            return counter;
        }

        @Override
        public void run() {
            counter = 0;
            while (handler.getPage(true) != BufferedWikiPageHandler.EOQ) {
                try {
                    Thread.sleep(0, random.nextInt(2) );
                } catch (InterruptedException ie) {
                    throw new IllegalStateException();
                }
                counter++;
            }
        }
    }

}
