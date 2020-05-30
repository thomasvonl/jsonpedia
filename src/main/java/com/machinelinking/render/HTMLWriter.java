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

import java.io.IOException;
import java.util.Map;

/**
 * Auxiliary class to write <i>HTML</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface HTMLWriter {

    void openDocument(String title) throws IOException;

    void closeDocument() throws IOException;

    void openTag(String tag) throws IOException;

    void openTag(String tag, Map<String,String> attributes) throws IOException;

    void closeTag() throws IOException;

    void heading(int level, String title) throws IOException;

    void html(String content) throws IOException;

    void text(String txt) throws IOException;

    void openColorTag(String color) throws IOException;

    void key(String key) throws IOException;

    void anchor(String url, String text, boolean internal) throws IOException;

    void anchor(String name) throws IOException;

    void image(String url, String text) throws IOException;

    void openList() throws IOException;

    void openListItem() throws IOException;

    void closeListItem() throws IOException;

    void closeList() throws IOException;

    void openTable(String title, Map<String,String> attributes) throws IOException;

    void openTableRow() throws IOException;

    void closeTableRow() throws IOException;

    void openTableCol() throws IOException;

    void closeTableCol() throws IOException;

    void tableRow(String... cols) throws IOException;

    void closeTable() throws IOException;

    void br() throws IOException;

    void em() throws IOException;

    void link(String description, String url) throws IOException;

    void reference(String description, String lang, String label, boolean internal) throws IOException;

    void templateReference(String description, String lang, String label) throws IOException;

    void category(String lang, String category) throws IOException;

    void span(String content) throws IOException;

    void flush() throws IOException;
}
