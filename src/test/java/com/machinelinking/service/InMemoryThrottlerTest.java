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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for {@link com.machinelinking.service.InMemoryThrottler}
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InMemoryThrottlerTest {

    @Test
    public void testThrottle() throws InterruptedException {
        final long limit = 500;
        final InMemoryThrottler throttler = new InMemoryThrottler(limit, 100000);
        int ELEMS = 50000;
        for(int i = 0; i < ELEMS; i++) {
            Assert.assertEquals(throttler.checkAllowed(Integer.toString(i)), 0);
        }
        Assert.assertTrue(throttler.isRunning());

        for (int i = 0; i < ELEMS; i++) {
            final long time = throttler.checkAllowed(Integer.toString(i));
            Assert.assertTrue(time > 0 && time <= limit);
        }
        Assert.assertTrue(throttler.isRunning());

        synchronized (this) {
            this.wait(limit * 3);
        }
        Assert.assertFalse(throttler.isRunning());

        for (int i = 0; i < ELEMS; i++) {
            Assert.assertEquals(throttler.checkAllowed(Integer.toString(i)), 0);
        }
        Assert.assertTrue(throttler.isRunning());

        synchronized (this) {
            this.wait(limit * 3);
        }
        Assert.assertFalse(throttler.isRunning());
    }

}
