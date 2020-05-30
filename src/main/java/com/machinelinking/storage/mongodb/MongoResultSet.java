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


import com.machinelinking.storage.ResultSet;
import com.mongodb.DBCursor;

import java.io.Closeable;

/**
 * {@link com.machinelinking.storage.ResultSet} implementation for <i>MongoDB</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoResultSet implements ResultSet<MongoDocument>, Closeable {

    private final DBCursor cursor;

    MongoResultSet(DBCursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public long getCount() {
        return cursor.count();
    }

    @Override
    public MongoDocument next() {
        return cursor.hasNext() ? MongoDocument.unwrap(cursor.next()) : null;
    }

    @Override
    public void close() {
        cursor.close();
    }

}
