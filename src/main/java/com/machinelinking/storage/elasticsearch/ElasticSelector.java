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

import com.machinelinking.storage.Criteria;
import com.machinelinking.storage.Selector;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.machinelinking.storage.Selector} implementation for <i>ElasticSearch</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticSelector implements Selector {

    private final List<Criteria> criterias = new ArrayList<>();

    @Override
    public void addCriteria(Criteria criteria) {
        criterias.add(criteria);
    }

    @Override
    public void addProjection(String field) {
        throw new UnsupportedOperationException();
    }

    //TODO: add full support for query string?
    // http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax
    public QueryBuilder buildQuery() {
        final QueryBuilder queryBuilder;
        if (criterias.isEmpty()) {
            queryBuilder = QueryBuilders.matchAllQuery();
        } else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (Criteria criteria : criterias) {
                if (criteria.operator != Criteria.Operator.eq)
                    throw new IllegalArgumentException("Unsupported operators different from [eq]");
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery(criteria.field == null ? "_all" : criteria.field, criteria.value)
                );
            }
            queryBuilder = boolQueryBuilder;
        }
        return queryBuilder;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for(Criteria criteria: criterias) {
            sb.append(criteria);
            if(i < criterias.size() - 2) sb.append(';');
            i++;
        }
        return sb.toString();
    }
}
