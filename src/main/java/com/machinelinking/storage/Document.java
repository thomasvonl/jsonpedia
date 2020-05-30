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

/**
 * Defines a <i>Wikitext</i> document.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface Document<T> {

    public static final String ID_FIELD = "_id";
    public static final String VERSION_FIELD = "version";
    public static final String NAME_FIELD = "name";
    public static final String CONTENT_FIELD = "content";
    public static final String[] FIELDS = new String[]{ID_FIELD, VERSION_FIELD, NAME_FIELD, CONTENT_FIELD};

    int getId();

    int getVersion();

    String getName();

    T getContent();

    String toJSON();

}
