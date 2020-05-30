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

package com.machinelinking.render;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultDocumentContext implements DocumentContext {

    private final String scope;
    private final URL documentURL;
    private final Map<String,String> vars;

    public DefaultDocumentContext(String scope, URL documentURL, Map<String, String> vars) {
        this.scope = scope;
        this.documentURL = documentURL;
        this.vars = vars;
    }

    public DefaultDocumentContext(String scope, URL documentURL) {
        this(scope, documentURL, Collections.<String, String>emptyMap());
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public URL getDocumentURL() {
        return documentURL;
    }

    @Override
    public String[] getVarNames() {
        return vars.keySet().toArray(new String[vars.keySet().size()]);
    }

    @Override
    public String getVar(String varName) {
        return vars.get(varName);
    }
}
