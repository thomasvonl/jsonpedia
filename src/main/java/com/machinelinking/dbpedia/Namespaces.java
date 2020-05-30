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

package com.machinelinking.dbpedia;

import java.util.HashMap;
import java.util.Map;

/**
 * Support for DBpedia Wikipedia namespaces.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Namespaces {

    private final Map<String,Integer> namespaces = new HashMap<String,Integer>(){{
        put("en", 204);
        put("de", 208);
        put("fr", 210);
        put("it", 212);
        put("es", 214);
        put("nl", 216);
        put("pt", 218);
        put("pl", 220);
        put("ru", 222);
        put("cs", 224);
        put("ca", 226);
        put("bn", 228);
        put("hi", 230);
        put("ja", 232);
        put("zh", 236);
        put("hu", 238);
        put("commons", 240);
        put("ko", 242);
        put("tr", 246);
        put("ar", 250);
        put("id", 254);
        put("sr", 256);
        put("sk", 262);
        put("bg", 264);
        put("sl", 268);
        put("eu", 272);
        put("eo", 274);
        put("et", 282);
        put("hr", 284);
        put("el", 304);
        put("be", 312);
        put("cy", 328);
        put("ur", 378);
        put("ga", 396);
    }};

    private static Namespaces instance;

    public static Namespaces getInstance() {
        if(instance == null) instance = new Namespaces();
        return instance;
    }

    private Namespaces() {}

    public int getNamespace(String lang) {
        return namespaces.get(lang);
    }

}
