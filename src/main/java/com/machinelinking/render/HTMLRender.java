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

import org.codehaus.jackson.JsonNode;

/**
 * Defines a <i>JSON</i> to <i>HTML</i> renderer.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface HTMLRender extends RootRender {

    /**
     * Registers a {@link NodeRender} for a specific type.
     *
     * @param type
     * @param render
     */
    void addNodeRender(String type, NodeRender render);

    /**
     * Deregisters a {@link NodeRender} for a specific type.
     *
     * @param type
     * @return <code>true</code> if removal succeeded, <code>false</code> otherwise.
     */
    boolean removeNodeRender(String type);

    /**
     * Registers a {@link KeyValueRender} for a given key.
     *
     * @param key
     * @param render
     */
    void addKeyValueRender(String key, KeyValueRender render);

    /**
     * Deregisters a {@link KeyValueRender} for a given key.
     *
     * @param key
     * @return <code>true</code> if removal succeeded, <code>false</code> otherwise.
     */
    boolean removeKeyValueRender(String key);

    /**
     * Registers a {@link PrimitiveNodeRender}.
     *
     * @param render
     */
    void addPrimitiveRender(PrimitiveNodeRender render);

    /**
     * Deregisters a {@link PrimitiveNodeRender}.
     *
     * @param render
     */
    void removePrimitiveRender(PrimitiveNodeRender render);

    /**
     * Renders a JSON node as a document.
     *
     * @param context
     * @param rootNode
     * @return
     * @throws NodeRenderException
     */

    String renderDocument(DocumentContext context, JsonNode rootNode) throws NodeRenderException;

    /**
     * Renders a JSON node as a fragment (without header and footer).
     *
     * @param context
     * @param node
     * @return
     * @throws NodeRenderException
     */
    String renderFragment(DocumentContext context, JsonNode node) throws NodeRenderException;

}
