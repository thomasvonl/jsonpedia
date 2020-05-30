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

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * Factory for {@link com.machinelinking.converter.ScriptableConverter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ScriptableConverterFactory {

    private static ScriptableConverterFactory instance;

    private final ScriptEngineManager engineManager;
    private final Bindings bindings;

    public static final ScriptableConverterFactory getInstance() {
        if(instance == null) {
            instance = new ScriptableConverterFactory();
        }
        return instance;
    }

    private ScriptableConverterFactory() {
        engineManager = new ScriptEngineManager();
        bindings = new SimpleBindings();
        final ScriptContextFunctions functions = new ScriptContextFunctions();
        bindings.put("functions", functions);
    }

    public ScriptableConverter createConverter(String script) throws ScriptableFactoryException {
        final ScriptEngine engine = engineManager.getEngineByName("python");
        engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        try {
            engine.eval(script);
            return new ScriptableConverter((Invocable)engine);
        } catch (ScriptException se) {
            throw new ScriptableFactoryException("Error while instantiating converter.", se);
        }
    }

}
