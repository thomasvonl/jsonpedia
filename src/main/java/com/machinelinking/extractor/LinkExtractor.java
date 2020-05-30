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

package com.machinelinking.extractor;

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.serializer.Serializer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific {@link Extractor} for <i>Wikipedia link</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class LinkExtractor extends SectionAwareExtractor {

    private List<Link> links;

    private URL url;
    private StringBuilder linkContent = new StringBuilder();
    private boolean foundParam;

    public LinkExtractor() {
        super(Ontology.LINKS_FIELD);
    }

    @Override
    public void beginLink(URL url) {
        this.url = url;
        linkContent.delete(0, linkContent.length());
        foundParam = false;
    }

    @Override
    public void parameter(String param) {
        if(foundParam) linkContent.append("|");
        foundParam = true;
        if(param != null) {
            linkContent.append(param).append("=");
        }
    }

    @Override
    public void text(String content) {
        linkContent.append(content);
    }

    @Override
    public void endLink(URL url) {
        // TODO: handle links exploded with templates, ex: [{{Allmusic|class=explore|id=style/d2693|pure_url=yes}} "Hair metal"]
        if(this.url == null) return;
        if(links == null) links = new ArrayList<>();
        links.add(new Link(this.url, linkContent.toString(), super.getSectionIndex()));
    }

    @Override
    public void flushContent(Serializer serializer) {
        if(links == null) {
            serializer.value(null);
            return;
        }
        serializer.openList();
        for(Link link : links) {
            link.serialize(serializer);
        }
        serializer.closeList();
        links.clear();
    }

    @Override
    public void reset() {
        if(links != null)links.clear();
    }

}
