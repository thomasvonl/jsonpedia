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

import com.machinelinking.wikimedia.WikiPage;
import org.codehaus.jackson.util.TokenBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiJSONStorageConnection implements JSONStorageConnection<MultiDocument,MultiSelector> {

    private final JSONStorageConnection[] connections;

    MultiJSONStorageConnection(JSONStorageConnection[] connections) {
        this.connections = connections;
    }

    @Override
    public MultiDocument createDocument(WikiPage page, TokenBuffer buffer) throws JSONStorageConnectionException {
        final List<Document> documents = new ArrayList<>();
        for(JSONStorageConnection connection : connections) {
            documents.add(connection.createDocument(page, buffer));
        }
        return new MultiDocument(
                connections,
                documents.toArray( new Document[documents.size()] )
        );
    }

    @Override
    public void addDocument(MultiDocument document) throws JSONStorageConnectionException {
        for(JSONStorageConnection connection : connections) {
            connection.addDocument(document.getDocumentForConnection(connection));
        }
    }

    @Override
    public void removeDocument(int id) throws JSONStorageConnectionException {
        for (JSONStorageConnection connection : connections) {
            connection.removeDocument(id);
        }
    }

    @Override
    public MultiDocument getDocument(int id) throws JSONStorageConnectionException {
        final List<Document> documents = new ArrayList<>();
        for (JSONStorageConnection connection : connections) {
            documents.add(connection.getDocument(id));
        }
        return new MultiDocument(
                connections,
                documents.toArray(new Document[documents.size()])
        );
    }

    @Override
    public long getDocumentsCount() throws JSONStorageConnectionException {
        float total = 0;
        for (JSONStorageConnection connection : connections) {
            total += connection.getDocumentsCount();
        }
        return (long) (total / connections.length);
    }

    @Override
    public ResultSet<MultiDocument> query(MultiSelector selector, int limit) throws JSONStorageConnectionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        for (JSONStorageConnection connection : connections) {
            connection.flush();
        }
    }

    @Override
    public void close() {
        for (JSONStorageConnection connection : connections) {
            connection.close();
        }
    }
}
