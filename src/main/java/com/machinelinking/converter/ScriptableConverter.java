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

package com.machinelinking.converter;

import com.machinelinking.serializer.Serializer;
import com.machinelinking.util.JSONUtils;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Scriptable implementation of {@link com.machinelinking.converter.Converter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ScriptableConverter implements Converter {

    public static final String CONVERT_DATA_METHOD = "convert_data";
    public static final String CONVERT_READABLE_METHOD = "convert_hr";

    private final Invocable invocableEngine;

    public ScriptableConverter(Invocable invocableEngine) {
        this.invocableEngine = invocableEngine;
    }

    @Override
    public void convertData(Map<String,?> data, Serializer serializer, Writer writer) throws ConverterException {
        final Object conversion = processMethod(CONVERT_DATA_METHOD, data);
        try {
            JSONUtils.jsonMapArrayToSerializer(conversion, serializer);
        } catch (Exception e) {
            throw new ConverterException("Error while converting conversion data to JSON.", e);
        }
        final String representation = processMethod(CONVERT_READABLE_METHOD, data).toString();
        try {
            writer.append(representation);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error while flusing readable data.", ioe);
        }
    }

    private Object processMethod(String method, Map<String,?> data) throws ConverterException {
        try {
            return invocableEngine.invokeFunction(method, data);
        } catch (ClassCastException cce) {
            throw new ConverterException("Invalid return value from " + method);
        } catch (ScriptException se) {
            throw new ConverterException("Error while evaluating " + method, se);
        } catch (NoSuchMethodException nsme) {
            throw new ConverterException("Script must implement " + method + " method.");
        }
    }

}
