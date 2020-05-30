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

package com.machinelinking.extractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a <i>Wikipedia table</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Table {

    private String identifier;
    private List<String> headers       = new ArrayList<String>();
    private List<List<String>> content = new ArrayList<List<String>>();

    public Table(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<String>> getContent() {
        return content;
    }

    protected void addHeader(String header) {
        headers.add(header);
    }

    protected void addRow(List<String> row) {
        content.add(row);
    }
}
