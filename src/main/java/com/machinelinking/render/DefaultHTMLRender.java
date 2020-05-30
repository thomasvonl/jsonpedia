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

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.util.DefaultJsonPathBuilder;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.util.JsonPathBuilder;
import com.machinelinking.wikimedia.WikimediaUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link HTMLRender}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultHTMLRender implements HTMLRender {

    private static final Map<String,String> CONTENT_STYLE = new HashMap<String,String>(){{
        put("class", "content");
    }};

    private static final Map<String,String> PRIMITIVE_NODE_ATTRS = new HashMap<String,String>(){{
        put("class", "root");
    }};

    private static final Map<String,String> TYPE_LABEL_ATTRS = new HashMap<String,String>(){{
        put("class", "type");
    }};

    private static final Map<String,String> OBJECT_NODE_ATTRS = new HashMap<String, String>(){{
        put("class","object");
    }};

    private static final Map<String,String> LIST_NODE_ATTRS = new HashMap<String, String>(){{
        put("class","list");
    }};

    private static final Map<String,String> DEFAULT_RENDER_NODE_ATTRS = new HashMap<String,String>(){{
    }};

    private static final Map<String,String> DEFAULT_RENDER_HIDDEN_NODE_ATTRS = new HashMap<String,String>(){{
        put("class", String.format("%s %s", DEFAULT_RENDER_CLASS, "default-render") );
    }};

    private static final String DEFAULT_RENDER_CLASS = "defaultrender";

    private final Map<String,List<NodeRender>> nodeRenders       = new HashMap<>();
    private final Map<String,KeyValueRender>   keyValueRenders   = new HashMap<>();
    private final List<PrimitiveNodeRender> primitiveNodeRenders = new ArrayList<>();

    private final JsonPathBuilder jsonPathBuilder = new DefaultJsonPathBuilder();

    private final boolean alwaysRenderDefault;

    private JsonContext context;

    public DefaultHTMLRender(boolean alwaysRenderDefault) {
        this.alwaysRenderDefault = alwaysRenderDefault;
    }

    public void processRoot(JsonNode node, HTMLWriter writer) throws NodeRenderException {
        jsonPathBuilder.startPath();
        try {
            writer.openDocument(getContext().getDocumentTitle());
            processFragment(reorderRoot(node), writer);
            writer.closeDocument();
            writer.flush();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

    public void processFragment(JsonNode node, HTMLWriter writer) throws NodeRenderException {
        render(getContext(), this, node, writer);
    }

    @Override
    public void addNodeRender(String type, NodeRender render) {
        List<NodeRender> rendersPerType = nodeRenders.get(type);
        if(rendersPerType == null) {
            rendersPerType = new ArrayList<>();
            nodeRenders.put(type, rendersPerType);
        }
        rendersPerType.add(render);
    }

    @Override
    public boolean removeNodeRender(String type) {
        return nodeRenders.remove(type) != null;
    }

    @Override
    public void addKeyValueRender(String key, KeyValueRender render) {
        if(keyValueRenders.containsKey(key)) throw new IllegalArgumentException();
        keyValueRenders.put(key, render);
    }

    @Override
    public boolean removeKeyValueRender(String key) {
        return keyValueRenders.remove(key) != null;
    }

    @Override
    public void addPrimitiveRender(PrimitiveNodeRender render) {
        primitiveNodeRenders.add(render);
    }

    @Override
    public void removePrimitiveRender(PrimitiveNodeRender render) {
        primitiveNodeRenders.remove(render);
    }

    @Override
    public boolean acceptNode(JsonContext context, JsonNode node) {
        return true;
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer)
    throws NodeRenderException {
        if(node == null) return;
        final JsonNode type = node.get(Ontology.TYPE_FIELD);
        NodeRender targetRender = null;
        if (type != null) {
            final List<NodeRender> rendersPerType = nodeRenders.get(type.asText());
            if (rendersPerType != null) {
                for(NodeRender nodeRender : rendersPerType) {
                    if(nodeRender.acceptNode(context, node)) {
                        targetRender = nodeRender;
                        break;
                    }
                }
            }
        }
        boolean renderFound = targetRender != null;

        // Node metadata.
        if(alwaysRenderDefault) try {
            writeNodeMetadata(node, writer);
        } catch (IOException ioe) {
            throw new NodeRenderException("Error while writing node metadata.", ioe);
        }

        // Custom node rendering.
        if(renderFound) {
            try {
                targetRender.render(context, rootRender, node, writer);
            } catch (Exception e) {
                renderFound = false;
            }
        }

        // Default node rendering.
        if(alwaysRenderDefault || !renderFound) {
            try {
                renderDefault(!renderFound, context, rootRender, node, writer);
            } catch (IOException ioe) {
                throw new NodeRenderException("Error while rendering default.", ioe);
            }
        }

        if(alwaysRenderDefault) try {
            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException("Error while closing tag.", ioe);
        }
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, String key, JsonNode value, HTMLWriter writer)
    throws NodeRenderException {
        try {
            final KeyValueRender render = keyValueRenders.get(key);
            if (render != null) {
                writer.anchor(key);
                render.render(context, rootRender, key, value, writer);
                return;
            }

            writer.openTag("div", CONTENT_STYLE);
            writer.openTag("span");
            writer.openTag("i");
            writer.text(key);
            writer.closeTag();
            writer.text(":");
            writer.closeTag();
            writer.openTag("span");
            render(context, rootRender, value, writer);
            writer.closeTag();
            writer.closeTag();
        } catch (IOException ioe) {
            throw new NodeRenderException("Error while processing rendering.", ioe);
        }
    }

    @Override
    public String renderDocument(DocumentContext context, JsonNode rootNode) throws NodeRenderException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DefaultHTMLWriter writer = new DefaultHTMLWriter( new OutputStreamWriter(baos) );
        this.setContext(rootNode, context);
        this.processRoot(rootNode, writer);
        try {
            writer.flush();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
        return baos.toString();
    }

    public String renderFragment(DocumentContext context, JsonNode rootNode) throws NodeRenderException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DefaultHTMLWriter writer = new DefaultHTMLWriter(new OutputStreamWriter(baos));
        this.setContext(rootNode, context);
        this.processFragment(rootNode, writer);
        try {
            writer.flush();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
        return baos.toString();
    }

    private JsonNode reorderRoot(JsonNode root) {
        ObjectNode newRoot = JSONUtils.getJsonNodeFactory().objectNode();
        Iterator<Map.Entry<String,JsonNode>> iter = root.getFields();
        Map.Entry<String,JsonNode> entry;
        final String pageStructField = Ontology.PAGE_DOM_FIELD;
        while(iter.hasNext()) {
            entry = iter.next();
            if(pageStructField.equals(entry.getKey())) continue;
            newRoot.put(entry.getKey(), entry.getValue());
        }
        newRoot.put(pageStructField, root.get(pageStructField));
        return newRoot;
    }

    private void setContext(final JsonNode root, final DocumentContext documentContext) {
         context = new JsonContext() {
             final URL documentURL = documentContext.getDocumentURL();
             final String documentTitle = WikimediaUtils.getEntityTitle(documentURL.toExternalForm());
             final String lang = documentURL.getHost().split("\\.")[0];
             final String domain = documentURL.getHost();

             @Override
             public DocumentContext getDocumentContext() {
                 return documentContext;
             }

             @Override
             public String getDocumentTitle() {
                 return documentTitle;
             }

             @Override
             public String getLang() {
                 return lang;
             }

             @Override
             public String getDomain() {
                 return domain;
             }

             @Override
             public String getJSONPath() {
                 return jsonPathBuilder.getJsonPath();
             }

             @Override
             public JsonNode getRoot() {
                 return root;
             }

             @Override
             public boolean subPathOf(JsonPathBuilder builder, boolean strict) {
                 return jsonPathBuilder.subPathOf(builder, strict);
             }
         };
    }

    private JsonContext getContext() {
        if(context == null) throw new IllegalStateException();
        return context;
    }

    private void renderDefault(
            boolean isDefault, JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer
    ) throws IOException, NodeRenderException {
                // Begin Default Render.
        writer.openTag("span");
        if (node.isObject()) {
            renderObject(rootRender, node, isDefault, writer);
        } else if (node.isArray()) {
            renderList(rootRender, node, isDefault, writer);
        } else {
            renderPrimitive(node, writer);
        }
        writer.closeTag();
        // End Default Render.
    }

    private void renderObject(RootRender rootRender, JsonNode obj, boolean isDefault, HTMLWriter writer)
    throws IOException, NodeRenderException {
        jsonPathBuilder.enterObject();
        writer.openTag("div", isDefault ? DEFAULT_RENDER_NODE_ATTRS : DEFAULT_RENDER_HIDDEN_NODE_ATTRS);
        final Iterator<Map.Entry<String,JsonNode>> iter = obj.getFields();
        Map.Entry<String,JsonNode> entry;
        while(iter.hasNext()) {
            entry = iter.next();
            if(Ontology.TYPE_FIELD.equals(entry.getKey())) continue;
            writer.openTag("div", OBJECT_NODE_ATTRS);
            jsonPathBuilder.field(entry.getKey());
            render(getContext(), rootRender, entry.getKey().trim(), entry.getValue(), writer);
            writer.closeTag();
        }
        jsonPathBuilder.exitObject();

        writer.closeTag();
    }

    private void renderList(RootRender rootRender, JsonNode list, boolean isDefault, HTMLWriter writer)
    throws IOException, NodeRenderException {
        final int size = list.size();
        if(size == 1) {
            jsonPathBuilder.enterArray();
            jsonPathBuilder.arrayElem();
            render(getContext(), rootRender, list.get(0), writer);
            jsonPathBuilder.exitArray();
            return;
        } else if(size == 0) {
            renderPrimitive(list, writer);
            return;
        }

        writer.openTag("div", LIST_NODE_ATTRS);
        writer.openTag("div", isDefault ? DEFAULT_RENDER_NODE_ATTRS : DEFAULT_RENDER_HIDDEN_NODE_ATTRS);
        jsonPathBuilder.enterArray();
        for(int i = 0; i < list.size(); i++) {
            jsonPathBuilder.arrayElem();
            writer.openTag("div");
            render(getContext(), rootRender, list.get(i), writer);
            writer.closeTag();
        }
        jsonPathBuilder.exitArray();
        writer.closeTag();
        writer.closeTag();
    }

    private void renderPrimitive(JsonNode node, HTMLWriter writer) throws IOException {
        for(PrimitiveNodeRender primitiveRender : primitiveNodeRenders) {
            if( primitiveRender.render(getContext(), node, writer) ) return;
        }

        final String primitive = JSONUtils.asPrimitiveString(node);
        if (primitive != null) {
            writer.openTag("span", PRIMITIVE_NODE_ATTRS);
            writer.text(primitive.trim());
            writer.closeTag();
        } else {
            writer.openTag("span");
            writer.text("&lt;empty&gt;");
            writer.closeTag();
        }
    }

    // TODO: should write any metadata available
    private String writeNodeMetadata(JsonNode node, HTMLWriter writer) throws IOException {
        final JsonNode typeNode = node.get(Ontology.TYPE_FIELD);
        final String type;
        if(typeNode == null) {
            type = null;
            writer.openTag("span", new HashMap<String,String>(){{put("title", jsonPathBuilder.getJsonPath());}});
        } else {
            type = typeNode.asText();
            final String name;
            name = "template".equals(type) ? node.get("name").asText() : null;
            writer.openTag("div", new HashMap<String, String>(){{
                put("itemtype", type);
                put("name"    , name);
                put("title", jsonPathBuilder.getJsonPath());
            }});

            writer.openTag("small", TYPE_LABEL_ATTRS);
            writer.text(type);
            writer.closeTag();
        }
        return type;
    }

}
