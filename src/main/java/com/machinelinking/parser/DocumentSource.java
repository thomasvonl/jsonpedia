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

package com.machinelinking.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Defines the source for a <i>Wikitext</i> page.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DocumentSource {

    private URL documentURL;
    private InputStream inputStream;

    public DocumentSource(URL documentURL, InputStream inputStream) {
        if(documentURL == null) throw new NullPointerException("documentURL cannot be null.");
        this.documentURL = documentURL;
        this.inputStream = inputStream;
    }

    public DocumentSource(URL documentURL, String wikitext) {
        if(documentURL == null) throw new NullPointerException("documentURL cannot be null.");
        if(wikitext == null) throw new NullPointerException("WikiText cannot be null");
        this.documentURL = documentURL;
        if(wikitext == null) {
            this.inputStream = null;
        } else {
            this.inputStream = new ByteArrayInputStream( wikitext.getBytes() );
        }
    }

    public DocumentSource(URL documentURL) {
        this(documentURL, (InputStream) null);
    }

    public URL getDocumentURL() {
        return documentURL;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}
