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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Map;

/**
 * Utility class for Mongo data conversions.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MongoUtils {

    private MongoUtils() {}

    public static JsonNode convertToJsonNode(BasicDBObject object) {
        final ObjectNode node = JsonNodeFactory.instance.objectNode();
        for(Map.Entry<String,Object> entry : object.entrySet()) {
            node.put(entry.getKey(), convertToJsonNode(entry.getValue()));
        }
        return node;
    }

    public static JsonNode convertToJsonNode(BasicDBList list) {
        final ArrayNode node = JsonNodeFactory.instance.arrayNode();
        for(Object entry : list) {
            node.add(convertToJsonNode(entry));
        }
        return node;
    }

    public static JsonNode convertToJsonNode(Object object) {
        if(object == null) return JsonNodeFactory.instance.nullNode();
        if(object instanceof BasicDBObject) return convertToJsonNode((BasicDBObject) object);
        if(object instanceof BasicDBList) return convertToJsonNode((BasicDBList) object);
        final Class objClass = object.getClass();
        if(String.class.equals(objClass)) return JsonNodeFactory.instance.textNode(object.toString());
        if(boolean.class.equals(objClass) || Boolean.class.equals(objClass))
            return JsonNodeFactory.instance.booleanNode((boolean) object);
        if(byte.class.equals(objClass) || Byte.class.equals(objClass))
            return JsonNodeFactory.instance.numberNode((byte) object);
        if(short.class.equals(objClass) || Short.class.equals(objClass))
            return JsonNodeFactory.instance.numberNode((short) object);
        if(int.class.equals(objClass) || Integer.class.equals(objClass))
            return JsonNodeFactory.instance.numberNode((int) object);
        if(long.class.equals(objClass) || Long.class.equals(objClass))
            return JsonNodeFactory.instance.numberNode((long) object);
        if(double.class.equals(objClass) || Double.class.equals(objClass))
            return JsonNodeFactory.instance.numberNode((double) object);
        throw new IllegalArgumentException("Unsupported conversion for object: " + object);
    }

}
