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
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiDocument implements Document {

    private final Document[] documents;
    private final JSONStorageConnection[] connections;

    protected MultiDocument(JSONStorageConnection[] connections, Document[] documents) {
        if(documents.length == 0) throw new IllegalArgumentException();
        if(documents.length != connections.length) throw new IllegalArgumentException();
        this.connections = connections;
        this.documents = documents;
    }

    public Document getDocumentForConnection(JSONStorageConnection c) {
        for(int i = 0; i < connections.length; i++) {
            if(connections[i] == c) return documents[i];
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getId() {
        return getSample().getId();
    }

    @Override
    public int getVersion() {
        return getSample().getVersion();
    }

    @Override
    public String getName() {
        return getSample().getName();
    }

    @Override
    public Object getContent() {
        return getSample().getContent();
    }

    @Override
    public String toJSON() {
        return getSample().toJSON();
    }

    private Document getSample() {
        return documents[0];
    }

}
