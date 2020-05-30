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

import com.machinelinking.pipeline.WikiPipelineFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Defines a processor to massive load a <i>Wikitext dump</i> stream into a {@link JSONStorage}
 * the {@link com.machinelinking.pipeline.WikiPipeline} built by the specified {@link com.machinelinking.pipeline.WikiPipelineFactory}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface JSONStorageLoader {

    WikiPipelineFactory getEnricherFactory();

    JSONStorage getStorage();

    StorageLoaderReport load(URL pagePrefix, InputStream is) throws IOException, SAXException;

}
