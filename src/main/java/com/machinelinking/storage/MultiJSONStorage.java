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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiJSONStorage implements JSONStorage<MultiJSONStorageConfiguration, MultiDocument, MultiSelector> {

    private final MultiJSONStorageConfiguration configuration;
    private final DocumentConverter<MultiDocument> converter;

    private final JSONStorage[] internalStorages;

    protected MultiJSONStorage(
            MultiJSONStorageConfiguration configuration,
            DocumentConverter<MultiDocument> converter,
            JSONStorage[] internalStorages
    ) {
        this.configuration = configuration;
        this.converter = converter;
        this.internalStorages = internalStorages;
    }

    public List<JSONStorage> getInternalStorages() {
        return new ArrayList<>(Arrays.asList(internalStorages));
    }

    @Override
    public MultiJSONStorageConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public DocumentConverter<MultiDocument> getConverter() {
        return converter;
    }

    @Override
    public boolean exists() {
        return exists(null);
    }

    @Override
    public boolean exists(String collection) {
        final String targetConnection = collection == null ? configuration.getCollection() : collection;
        boolean exist, notexist; exist = notexist = false;
        for (JSONStorage storage : internalStorages) {
            if(storage.exists(targetConnection))
                exist = true;
            else
                notexist = true;
        }
        if(exist && notexist)throw new IllegalStateException();
        return exist;
    }

    @Override
    public JSONStorageConnection<MultiDocument, MultiSelector> openConnection() {
        return openConnection(null);
    }

    @Override
    public MultiJSONStorageConnection openConnection(String collection) {
        final String targetCollection = collection == null ? configuration.getCollection() : collection;
        final List<JSONStorageConnection> connections = new ArrayList<>();
        for(JSONStorage storage : internalStorages) {
            connections.add(storage.openConnection(targetCollection));
        }
        return new MultiJSONStorageConnection(connections.toArray(new JSONStorageConnection[connections.size()]));
    }

    @Override
    public void deleteCollection() {
        deleteCollection(null);
    }

    @Override
    public void deleteCollection(String collection) {
        final String targetConnection = collection == null ? configuration.getCollection() : collection;
        for(JSONStorage storage : internalStorages) {
            storage.deleteCollection(targetConnection);
        }
    }

    @Override
    public void close() {
        for(JSONStorage storage : internalStorages) {
            storage.close();
        }
    }

}
