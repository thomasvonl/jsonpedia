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

package com.machinelinking.service;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In memory implementation for {@link com.machinelinking.service.Throttler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InMemoryThrottler implements Throttler {

    private static final Logger logger = Logger.getLogger(InMemoryThrottler.class);

    private static final int DEFAULT_SIZE_LIMIT_WARNING = 500; // 500 * (((3*4) + 3) * 2 + 4) bytes

    private final Map<String,Long> lastAccess = Collections.synchronizedMap(new HashMap<String, Long>());

    private final Object waitObj = new Object();

    private final long limit;

    private final int sizeLimitWarning;

    private final Disposer disposer;

    private Thread disposerThread;

    public InMemoryThrottler(long limit, int sizeLimitWarning) {
        this.limit = limit;
        this.sizeLimitWarning = sizeLimitWarning;
        disposer = new Disposer();
    }

    public InMemoryThrottler(long limit) {
        this(limit, DEFAULT_SIZE_LIMIT_WARNING);
    }

    /**
     * Verifies that the disposer thread is not running.
     *
     * @return <code>true</code> if disposer thread is running.
     */
    public boolean isRunning() {
        synchronized (this) {
            return disposerThread != null;
        }
    }

    @Override
    public long checkAllowed(String ip) {
        final Long last = lastAccess.get(ip);
        if(last == null) {
            lastAccess.put(ip, System.currentTimeMillis());
            checkRunning();
            if(lastAccess.size() >= sizeLimitWarning) {
                logger.warn(String.format(
                        "Number of entries in cache exceeded warning limit: %d [%d]",
                        lastAccess.size(), sizeLimitWarning
                ));
            }
            return 0;
        } else {
            final long elapsed = System.currentTimeMillis() - last;
            return elapsed >= limit ? 0 : limit - elapsed;
        }
    }

    private void checkRunning() {
        synchronized (this) {
            if (disposerThread == null) {
                disposerThread = new Thread(disposer, InMemoryThrottler.class.getName() + "-disposer");
                disposerThread.start();
            }
        }
    }

    private class Disposer implements Runnable {
        @Override
        public void run() {
            while(true) {
                synchronized (waitObj) {
                    try {
                        waitObj.wait(limit);
                    } catch (InterruptedException ie) {
                        throw new RuntimeException("Interrupted.", ie);
                    }
                }
                final long curr = System.currentTimeMillis();
                final List<String> removing = new ArrayList<>();
                for (Map.Entry<String, Long> entry : lastAccess.entrySet()) {
                    if (curr - entry.getValue() > limit) {
                        removing.add(entry.getKey());
                    }
                }
                for (String k : removing) {
                    lastAccess.remove(k);
                }
                synchronized (this) {
                    if (lastAccess.isEmpty()) {
                        disposerThread = null;
                        break;
                    }
                }
            }
        }
    }

}
