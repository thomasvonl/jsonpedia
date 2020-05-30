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

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A <i>Wikipage</i> processing queue implementing {@link WikiPageHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class BufferedWikiPageHandler implements WikiPageHandler {

    public static final int MAX_BUFFER_SIZE = 1024 * 1024; // 1MB

    public static final WikiPage EOQ = new WikiPage(0, 0, null, null);

    private final StringBuilder sb = new StringBuilder();
    private final Stack<WikiPage> buffer = new Stack<>();

    private AtomicBoolean closed = new AtomicBoolean(false);

    private int bufferSize = 0;

    private int     id;
    private int     revId;
    private String  title;

    public int size() {
        synchronized (buffer) {
            return buffer.size();
        }
    }

    public int sizeChars() {
        synchronized (buffer) {
            return bufferSize;
        }
    }

    public WikiPage getPage(boolean wait) {
        synchronized (buffer) {
            if (buffer.isEmpty() && closed.get()) {
                return EOQ;
            }

            WikiPage page;
            while (true) {
                page = take();
                if (page == null) {
                    if (wait) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException("Error while waiting for buffer filling.", ie);
                        }
                    } else {
                        return null;
                    }
                } else {
                    buffer.notifyAll();
                    return page;
                }
            }
        }
    }

    public void reset() {
        sb.delete(0, sb.length());
        synchronized (buffer) {
            buffer.clear();
            bufferSize = 0;
            closed.set(false);
        }
    }

    @Override
    public void startStream() {
        reset();
    }

    @Override
    public void startWikiPage(int id, int revisionId, String title) {
        this.id = id;
        this.revId = revisionId;
        this.title = title;
    }

    @Override
    public void wikiPageContent(char[] buffer, int offset, int len) {
        sb.append(buffer, offset, len);
    }

    @Override
    public void endWikiPage() {
        final String content = sb.toString();
        sb.delete(0, sb.length());
        synchronized (buffer) {
            final WikiPage page = new WikiPage(this.id, this.revId, this.title, content);
            buffer.push(page);
            bufferSize += page.getSize();
            buffer.notifyAll();
            while (bufferSize >= MAX_BUFFER_SIZE) try {
                buffer.notifyAll();
                buffer.wait();
            } catch (InterruptedException ie) {
                throw new RuntimeException("Error while waiting for max size unlock.", ie);
            }
        }
    }

    @Override
    public void endStream() {
        closed.set(true);
        synchronized (buffer) {
            buffer.notifyAll();
        }
    }

    private WikiPage take() {
        if(buffer.isEmpty()) return null;
        final WikiPage page = buffer.pop();
        bufferSize -= page.getSize();
        return page;
    }

}
