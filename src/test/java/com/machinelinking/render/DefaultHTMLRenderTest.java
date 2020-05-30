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

package com.machinelinking.render;

import com.machinelinking.template.RenderScope;
import com.machinelinking.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Test case for {@link DefaultHTMLRender}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultHTMLRenderTest {

    @Test
    public void testRender() throws IOException, NodeRenderException {
        final JsonNode node = JSONUtils.parseJSON(
                this.getClass().getResourceAsStream(
                        "Enrichment1.json"
                )
        );
        final URL documentURL = new URL("http://en.wikipedia.org/page/Fake");
        final DocumentContext context = new DefaultDocumentContext(RenderScope.FULL_RENDERING, documentURL);
        final String html = DefaultHTMLRenderFactory.getInstance().createRender().renderDocument(context, node);
        FileUtils.writeStringToFile( new File("./test-render.html"), html);
    }

}
