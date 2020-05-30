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

package com.machinelinking.storage.elasticsearch;

import com.machinelinking.storage.Document;
import com.machinelinking.util.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link com.machinelinking.storage.Document} implementation for <i>ElasticSearch</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticDocument implements Document<Map<String,Object>> {

    private final Map<String,Object> document;

    public static ElasticDocument unwrap(Map<String,Object> in) {
        if(in == null) return null;
        if(
            in.containsKey(ID_FIELD) &&
            in.containsKey(VERSION_FIELD) &&
            in.containsKey(NAME_FIELD) &&
            in.containsKey(CONTENT_FIELD)
        ) return new ElasticDocument(in);
        else throw new IllegalArgumentException();
    }

    public ElasticDocument(int id, Integer version, String name, Map<String,?> content) {
        this.document = new HashMap<>();
        this.document.put(ID_FIELD, id);
        if(version != null) this.document.put(VERSION_FIELD, version);
        if(name != null) this.document.put(NAME_FIELD, name);
        if(content != null) this.document.put(CONTENT_FIELD, content);
    }

    private ElasticDocument(Map<String,Object> in) {
        this.document = in;
    }

    public Map<String,Object> getInternal() {
        return document;
    }

    @Override
    public int getId() {
        return (int) document.get(ID_FIELD);
    }

    @Override
    public int getVersion() {
        return (int) document.get(VERSION_FIELD);
    }

    @Override
    public String getName() {
        return (String) document.get(NAME_FIELD);
    }

    @Override
    public Map<String,Object> getContent() {
        return (Map<String,Object>) document.get(CONTENT_FIELD);
    }

    @Override
    public String toJSON() {
        return JSONUtils.toHumanReadable(document);
    }

}
