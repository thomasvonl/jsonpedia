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

package com.machinelinking.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows to retrieve fields hidden in classes not directly exposed by public methods.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Probe {

    private static final Probe instance = new Probe();

    public static Probe getInstance() {
        return instance;
    }

    private final Map<String,Field[]> accessors = new HashMap<>();

    private Probe() {}

    public synchronized <C> C probePath(Object root, String path) {
        final String[] pathlist = path.split("\\.");
        final Field[] fields = accessors.get(objectPathToId(root, pathlist));
        if(fields != null) {
            return (C) follow(root, fields);
        } else {
            return (C) followAndCache(root, pathlist);
        }
    }

    private Object follow(Object root, Field[] fields){
        Object curr = root;
        for(Field field : fields) {
            try {
                curr = field.get(curr);
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException();
            }
        }
        return curr;
    }

    private Object followAndCache(Object root, String[] path) {
        Field field;
        Object curr = root;
        List<Field> fields = new ArrayList<>();
        for (String part : path) {
            try {
                try {
                    field = curr.getClass().getDeclaredField(part);
                } catch (NoSuchFieldException nfe) {
                    field = curr.getClass().getSuperclass().getDeclaredField(part);
                }
                if(field == null) throw new NoSuchFieldException();
                field.setAccessible(true);
                fields.add(field);
                curr = field.get(curr);
            } catch (NoSuchFieldException nfe) {
                throw new IllegalArgumentException(
                        String.format("Cannot find field %s in path %s", part, Arrays.toString(path))
                );
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException(iae);
            }
        }
        accessors.put(objectPathToId(root, path), fields.toArray(new Field[fields.size()]));
        return curr;
    }

    private String objectPathToId(Object o, String[] path) {
        return String.format("%s-%s", o.getClass(), Arrays.toString(path));
    }

}
