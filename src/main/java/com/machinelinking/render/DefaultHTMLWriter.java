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

import com.machinelinking.extractor.Reference;
import com.machinelinking.wikimedia.WikimediaUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Default implementation of {@link HTMLWriter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultHTMLWriter implements HTMLWriter {

    private static final String PACKAGE_RESOURCES = DefaultHTMLWriter.class.getPackage().getName().replace(".", "/");

    private final Map<String,String> fileCache = new HashMap<>();

    private final Writer writer;

    private Stack<String> openTags = new Stack<>();

    public DefaultHTMLWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void openDocument(String title) throws IOException {
        writer.append(loadResource("page-open"));
        writer.append(expand("page-title", title, loadResource("header")));
        writer.append(loadResource("body-open"));
        writer.append(loadResource("body"));
    }

    @Override
    public void closeDocument() throws IOException {
        writer.append(loadResource("body-close"));
        writer.append(loadResource("page-close"));
        if(!openTags.isEmpty()) throw new IllegalStateException();
    }

    @Override
    public void openTag(String tag, Map<String, String> attributes) throws IOException {
        writer.append('<');
        writer.append(tag);
        if(attributes != null) {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                writer.append(' ');
                writer.append(attribute.getKey());
                final String value = attribute.getValue();
                if(value != null)
                writer.append("=\"").append(value).append('"');
            }
        }
        writer.append('>');
        openTags.push(tag);
    }

    @Override
    public void openTag(String tag) throws IOException {
        openTag(tag, null);
    }

    @Override
    public void closeTag() throws IOException {
        final String tag;
        try {
            tag = openTags.pop();
        } catch (Exception e) {
            throw new RuntimeException("Too many close tags.", e);
        }
        writer.append("</");
        writer.append(tag);
        writer.append('>');
    }

    @Override
    public void heading(int level, String title) throws IOException {
        if(level > 6) level = 6;
        writer.append(String.format("<h%d>%s</h%d>", level, title, level));
    }

    @Override
    public void html(String content) throws IOException {
        writer.append(content);
    }

    @Override
    public void text(String txt) throws IOException {
        html(escapeStringMarkup(txt));
    }

    @Override
    public void openColorTag(final String color) throws IOException {
        openTag("font", new HashMap<String, String>() {{
            put("color", color);
        }});
    }

    @Override
    public void key(String txt) throws IOException {
        writer.append( String.format("<small>%s</small>: ", txt) );
    }

    @Override
    public void anchor(String url, String text, boolean internal) throws IOException {
        if(internal) {
            url = Reference.toInternalURLString(url);
        }
        writer.append( String.format("<a href=\"%s\">%s</a>", url, text) );
    }

    @Override
    public void anchor(String name) throws IOException {
        writer.append(String.format("<a name=\"%s\"></a>", name));
    }

    @Override
    public void image(String url, String text) throws IOException {
        writer.append( String.format("<a href=\"%s\"><img src=\"%s\"/ alt=\"%s\"/></a>", url, url, text) );
    }

    @Override
    public void openList() throws IOException {
        openTag("ul");
    }

    @Override
    public void openListItem() throws IOException {
        openTag("li");
    }

    @Override
    public void closeListItem() throws IOException {
        closeTag();
    }

    @Override
    public void closeList() throws IOException {
        closeTag();
    }

    @Override
    public void openTable(String title, Map<String,String> attributes) throws IOException {
        final Map<String,String> finalAttrs = new HashMap<>(
                attributes == null ? Collections.<String,String>emptyMap() : attributes
        );
        merge("style", "border: 1px solid black", finalAttrs);
        openTag("table", finalAttrs);
        writer.append("<caption>");
        writer.append(title);
        writer.append("</caption>");
    }

    @Override
    public void openTableRow() throws IOException {
        writer.append("<tr>");
    }

    @Override
    public void closeTableRow() throws IOException {
        writer.append("</tr>");
    }

    @Override
    public void openTableCol() throws IOException {
        writer.append("<td>");
    }

    @Override
    public void closeTableCol() throws IOException {
        writer.append("</td>");
    }

    @Override
    public void tableRow(String... cols) throws IOException {
        if(cols.length == 0) return;
        openTableRow();
        for(String col : cols) {
            openTableCol();
            writer.append(col);
            closeTableCol();
        }
        closeTableRow();
    }

    @Override
    public void closeTable() throws IOException {
        closeTag();
    }

    @Override
    public void br() throws IOException {
        writer.append("<br/>");
    }

    @Override
    public void em() throws IOException {
        openTag("em");
    }

    @Override
    public void link(String description, String url) throws IOException {
        writer.append(String.format(
                "<a class=\"links-link\" target=\"_blank\" href=\"%s\">%s</a>",
                url,
                description.replaceAll("[\"']", "")
        ));
    }

    @Override
    public void reference(String description, String lang, String label, boolean internal) throws IOException {
        writer.append(String.format(
                "<a class=\"references-link\" %s href=\"%s\">%s</a>",
                internal ? "" : "target=\"_blank\"",
                internal ? Reference.toInternalURLString(lang, label) : Reference.toURLString(lang, label),
                description.replaceAll("[\"']", "")
        ));
    }

    @Override
    public void templateReference(String description, String lang, String label) throws IOException {
        writer.append(String.format(
                "<a class=\"templates-link\" target=\"_blank\" href=\"%s\">%s</a>",
                WikimediaUtils.toTemplateURL(lang, label).toExternalForm(),
                description
        ));
    }

    @Override
    public void category(String lang, String category) throws IOException {
        writer.append(String.format(
                "<a class=\"categories-link\" target=\"_blank\" href=\"%s\">%s</a>",
                WikimediaUtils.toCategoryURL(lang, category).toExternalForm(),
                category
        ));
    }

    @Override
    public void span(String content) throws IOException {
        openTag("span");
        text(content);
        closeTag();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    private String loadResource(String resource) throws IOException {
        String fileContent = fileCache.get(resource);
        if(fileContent != null) return fileContent;

        fileContent = IOUtils.toString(
                this.getClass().getResourceAsStream(
                        String.format("/%s/default-html-writer-%s.html", PACKAGE_RESOURCES, resource)
                )
        );
        fileCache.put(resource, fileContent);
        return fileContent;
    }

    private String expand(String key, String value, String context) {
        return context.replaceAll(String.format("\\$\\{%s\\}", key), value);
    }

    private String escapeStringMarkup(String in) {
        return in.replace("<", "&lt;").replace(">", "&gt").replace("&nbsp", " ");
    }

    private void merge(String key, String val, Map<String,String> inOut) {
        if(inOut.containsKey(key)) {
            if("style".equals(key)) {
                inOut.put(key, inOut.get(key) + "; " + val);
            } else {
                throw new IllegalArgumentException("Unsupported attribute merge for key " + key);
            }
        } else {
            inOut.put(key, val);
        }
    }

}
