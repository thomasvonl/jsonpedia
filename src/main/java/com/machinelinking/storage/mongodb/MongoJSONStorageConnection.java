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

package com.machinelinking.storage.mongodb;

import com.machinelinking.storage.DocumentConverter;
import com.machinelinking.storage.JSONStorageConnection;
import com.machinelinking.storage.JSONStorageConnectionException;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.WikiPage;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.util.JSON;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.util.TokenBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link com.machinelinking.storage.JSONStorageConnection} for <i>MongoDB</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoJSONStorageConnection implements JSONStorageConnection<MongoDocument, MongoSelector>, MapReduceSupport {

    public static final int BUFFER_FLUSH_SIZE = 1024;

    private final DBCollection collection;

    private final DocumentConverter<MongoDocument> converter;

    private final List<DBObject> buffer = new ArrayList<>();

    protected MongoJSONStorageConnection(DBCollection collection, DocumentConverter<MongoDocument> converter) {
        this.collection = collection;
        this.converter = converter;
    }

    @Override
    public MongoDocument createDocument(WikiPage page, TokenBuffer buffer) throws JSONStorageConnectionException {
        final DBObject dbNode = (DBObject) JSON.parse(JSONUtils.bufferToJSONString(buffer, false)); //TODO: improve this serialization
        return new MongoDocument(page.getId(), page.getRevId(), page.getTitle(), dbNode);
    }

    @Override
    public void addDocument(MongoDocument in) {
        final MongoDocument document = converter == null ? in : converter.convert(in);
        buffer.add(document.getInternal());
        flushBuffer(false);
    }

    @Override
    public void removeDocument(int id) {
        collection.remove( new MongoDocument(id, null, null, null).getInternal() );
    }

    @Override
    public MongoDocument getDocument(int id) {
        final DBObject found = collection.findOne( new MongoDocument(id, null, null, null).getInternal() );
        return MongoDocument.unwrap(found);
    }

    @Override
    public long getDocumentsCount() {
        return collection.count();
    }

    @Override
    public MongoResultSet query(MongoSelector selector, int limit) throws JSONStorageConnectionException {
        try {
            return new MongoResultSet(
                    collection.find(
                            selector.toDBObjectSelection(),
                            selector.toDBObjectProjection()
                    ).limit(limit)
            );
        } catch (Exception e) {
            throw new JSONStorageConnectionException("Error while performing query.", e);
        }
    }

    @Override
    public void flush() {
        flushBuffer(true);
    }

    @Override
    public void close() {
        flushBuffer(true);
    }

    private void flushBuffer(boolean force) {
        if(force || buffer.size() > BUFFER_FLUSH_SIZE) {
            //this.collection.insert(buffer);
            for(DBObject doc : buffer) {
                this.collection.save(doc);
            }
            buffer.clear();
        }
    }

    @Override
    public JsonNode processMapReduce(DBObject query, String map, String reduce, int limit) {
        final MapReduceCommand command = new MapReduceCommand(
                this.collection, map, reduce, null, MapReduceCommand.OutputType.INLINE, query
        );
        command.setLimit(limit);
        final MapReduceOutput out = this.collection.mapReduce(command);
        final DBObject results = (DBObject) out.getCommandResult().get("results");
        return MongoUtils.convertToJsonNode(results);
    }

}
