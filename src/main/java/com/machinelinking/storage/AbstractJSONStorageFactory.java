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
public abstract class AbstractJSONStorageFactory<C extends JSONStorageConfiguration, S extends JSONStorage, D extends Document> implements JSONStorageFactory<C,S, D> {

    protected Configuration parseConfigurationURI(String configURI) {
        final String[] sections = configURI.split(":");
        if (sections.length != 4)
            throw new IllegalArgumentException("Invalid config URI: must be: <host>:<port>:<db>:<collection>");
        final String host = checkValid(sections[0], "host");
        final int port;
        try {
            port = Integer.parseInt(sections[1]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid port, must be a number: " + sections[1]);
        }
        final String db = checkValid(sections[2], "db");
        final String collection = checkValid(sections[3], "collection");
        return new Configuration(host, port, db, collection);
    }

    private String checkValid(String in, String desc) {
        if(in.trim().length() == 0) {
            throw new IllegalArgumentException(String.format("Invalid value '%s' for %s", in, desc));
        }
        return in;
    }

    protected class Configuration {
        public final String host;
        public final int port;
        public final String db;
        public final String collection;
        Configuration(String host, int port, String db, String collection) {
            this.host = host;
            this.port = port;
            this.db = db;
            this.collection = collection;
        }
    }

}
