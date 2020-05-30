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

import com.machinelinking.storage.ResultSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ElasticResultSet implements ResultSet<ElasticDocument>, Closeable {

    private final String explain;
    private final SearchResponse response;
    private final Iterator<SearchHit> hits;

    public ElasticResultSet(String explain, SearchResponse response) {
        this.explain = explain;
        this.response = response;
        this.hits= response.getHits().iterator();
    }

    public String getExplain() {
        return explain;
    }

    @Override
    public long getCount() {
        return response.getHits().getTotalHits();
    }

    @Override
    public ElasticDocument next() {
        if(hits.hasNext()) {
            return ElasticDocument.unwrap(hits.next().getSource());
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        // Empty.
    }

}
