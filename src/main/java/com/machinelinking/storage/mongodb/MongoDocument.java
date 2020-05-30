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

import com.machinelinking.storage.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * {@link com.machinelinking.storage.Document} implementation for {@link DBObject}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoDocument implements Document<DBObject> {

    private final DBObject dbObject;

    public static MongoDocument unwrap(DBObject in) {
        if(in == null) return null;
        if(
            in.containsField(ID_FIELD) &&
            in.containsField(VERSION_FIELD) &&
            in.containsField(NAME_FIELD) &&
            in.containsField(CONTENT_FIELD)
        ) return new MongoDocument(in);
        else throw new IllegalArgumentException();
    }

    public MongoDocument(int id, Integer version, String name, DBObject content) {
        this.dbObject = new BasicDBObject();
        this.dbObject.put(ID_FIELD, id);
        if(version != null) this.dbObject.put(VERSION_FIELD, version);
        if(name != null) this.dbObject.put(NAME_FIELD, name);
        if(content != null) this.dbObject.put(CONTENT_FIELD, content);
    }

    private MongoDocument(DBObject content) {
        this.dbObject = content;
    }

    public DBObject getInternal() {
        return this.dbObject;
    }

    @Override
    public int getId() {
        return (int) dbObject.get(ID_FIELD);
    }

    @Override
    public int getVersion() {
        return (int) dbObject.get(VERSION_FIELD);
    }

    @Override
    public String getName() {
        return (String) dbObject.get(NAME_FIELD);
    }

    @Override
    public DBObject getContent() {
        return (DBObject) dbObject.get(CONTENT_FIELD);
    }

    @Override
    public String toJSON() {
        return JSON.serialize(getContent());
    }

}
