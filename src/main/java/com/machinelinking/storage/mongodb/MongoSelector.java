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

import com.machinelinking.storage.Criteria;
import com.machinelinking.storage.Selector;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link com.machinelinking.storage.Selector} implementation for <i>MongoDB</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoSelector implements Selector {

    private List<Criteria> criterias = new ArrayList<>();
    private Set<String> projections = new HashSet<>(Arrays.asList(MongoDocument.FIELDS));

    @Override
    public void addCriteria(Criteria criteria) {
        criterias.add(criteria);
    }

    @Override
    public void addProjection(String field) {
        projections.add(field);
    }

    Criteria[] getCriterias() {
        return criterias.toArray( new Criteria[criterias.size()] );
    }

    String[] getProjections() {
        return projections.toArray(new String[projections.size()]);
    }

    public DBObject toDBObjectSelection() {
        final DBObject obj = new BasicDBObject();
        for(Criteria criteria : this.getCriterias()) {
            obj.put(criteria.field, toValue(criteria.operator, criteria.value));
        }
        return obj;
    }

    public DBObject toDBObjectProjection() {
        final DBObject obj = new BasicDBObject();
        for(Criteria criteria : this.getCriterias()) {
            obj.put(criteria.field, 1);
        }
        for(String projection : this.getProjections()) {
            obj.put(projection, 1);
        }
        return obj;
    }

    @Override
    public String toString() {
        return String.format("criterias: %s, projections: %s", criterias.toString(), projections.toString());
    }

    private Object toValue(Criteria.Operator operator, Object value) {
        switch (operator) {
            case eq:
                return value;
            case neq:
                return new BasicDBObject("$neq", value);
            case gt:
                return new BasicDBObject("$gt", value);
            case gte:
                return new BasicDBObject("$gte", value);
            case lt:
                return new BasicDBObject("$lt", value);
            case lte:
                return new BasicDBObject("$lte", value);
            default:
                throw new UnsupportedOperationException();
        }
    }

}
