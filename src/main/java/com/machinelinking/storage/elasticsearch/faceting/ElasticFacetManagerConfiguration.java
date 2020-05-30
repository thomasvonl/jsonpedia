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

package com.machinelinking.storage.elasticsearch.faceting;

import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;

import java.util.Objects;

/**
 * Configuration of {@link com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManager}
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface ElasticFacetManagerConfiguration {

    interface Attribute {
        String toValue();
    }

    enum PropertyType implements Attribute {
         string {
             @Override
             public String toValue() {
                 return "string";
             }
         },
        _long {
             @Override
             public String toValue() {
                 return "long";
             }
         }
    }

    enum Analyzer implements Attribute {
        not_analyzed {
            @Override
            public String toValue() {
                throw new IllegalStateException();
            }
        },
        custom_lowercase {
            @Override
            public String toValue() {
                return "custom_lowercase";
            }
        },
        custom_kstem {
            @Override
            public String toValue() {
                return "custom_kstem";
            }
        }
    }

    ElasticJSONStorage getSourceStorage();

    ElasticJSONStorage getDestinationStorage();

    int getLimit();

    Property[] getProperties();

    class Property {
        public final String indexName;
        public final String field;
        public final PropertyType type;
        public final Analyzer analyzer;

        public Property(String indexName, String field, PropertyType type, Analyzer analyzer) {
            Objects.requireNonNull(indexName);
            Objects.requireNonNull(field);
            Objects.requireNonNull(type);
            Objects.requireNonNull(analyzer);
            this.indexName = indexName;
            this.field = field;
            this.type = type;
            this.analyzer = analyzer;
        }

        @Override
        public String toString() {
            return String.format("%s: [%s] %s %s", indexName, field, type, analyzer);
        }
    }

}
