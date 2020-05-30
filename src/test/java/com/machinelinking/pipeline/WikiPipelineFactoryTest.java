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

package com.machinelinking.pipeline;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test case for {@link WikiPipelineFactory}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiPipelineFactoryTest {

    @Test
    public void testToFlagsJustDefault() {
        final Flag[] DEFAULTS = new Flag[]{ WikiPipelineFactory.Structure, WikiPipelineFactory.Validate };
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("", DEFAULTS)),
                Arrays.asList(DEFAULTS)
        );
    }

    @Test
    public void testToFlagsOptionalAdded() {
        final Flag[] DEFAULTS = new Flag[]{ WikiPipelineFactory.Structure, WikiPipelineFactory.Validate };
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("Linkers", DEFAULTS)),
                Arrays.asList(
                        WikiPipelineFactory.Structure, WikiPipelineFactory.Validate, WikiPipelineFactory.Linkers
                )
        );

        // Lowercase.
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("linkers", DEFAULTS)),
                Arrays.asList(
                        WikiPipelineFactory.Structure, WikiPipelineFactory.Validate, WikiPipelineFactory.Linkers
                )
        );
    }

    @Test
    public void testToFlagsDefaultRemoved() {
        final Flag[] DEFAULTS = new Flag[]{ WikiPipelineFactory.Structure, WikiPipelineFactory.Validate };
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("-Validate", DEFAULTS)),
                Arrays.asList(WikiPipelineFactory.Structure)
        );

        // Lowercase.
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("-validate", DEFAULTS)),
                Arrays.asList(WikiPipelineFactory.Structure)
        );
    }

    @Test
    public void testToFlagsOptionalAddedDefaultRemoved() {
        final Flag[] DEFAULTS = new Flag[]{ WikiPipelineFactory.Structure, WikiPipelineFactory.Validate };
        Assert.assertEquals(
                Arrays.asList(WikiPipelineFactory.getInstance().toFlags("Linkers,-Structure", DEFAULTS)),
                Arrays.asList(WikiPipelineFactory.Validate, WikiPipelineFactory.Linkers)
        );
    }

}
