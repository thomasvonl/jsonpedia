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

import com.machinelinking.wikimedia.WikiPageHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * A parser for the DBpedia Mappings Wikipage XML API response.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiMappingsDumpParser extends DefaultHandler {

    private static final String PAGE_NODE     = "page";
    private static final String REVISION_NODE = "rev";
    private static final String ALLPAGES_NODE = "allpages";
    private static final String ID_ATTR       = "pageid";
    private static final String TITLE_ATTR    = "title";
    private static final String REV_ATTR      = "revid";
    private static final String GAPFROM_ATTR  = "gapfrom";

    private final SAXParserFactory factory = SAXParserFactory.newInstance();
    private final SAXParser saxParser;

    private WikiPageHandler handler;

    private boolean insidePage = false;
    private boolean insideRevision = false;

    private Integer pageId   = null;
    private Integer pageRev  = null;
    private String pageTitle = null;
    private String gapFrom   = null;

    public WikiMappingsDumpParser() {
        try {
            saxParser = factory.newSAXParser();
        } catch (Exception e) {
            throw new IllegalStateException("Error while initializing Dump parser.", e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        handler.startStream();
    }

    @Override
    public void endDocument() throws SAXException {
        handler.endStream();
    }

    public String parse(WikiPageHandler handler, InputStream is) throws IOException, SAXException {
        this.handler = handler;
        insidePage = insideRevision = false;
        gapFrom = null;
        saxParser.parse(is, this);
        return gapFrom;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException {
        if(gapFrom == null && ALLPAGES_NODE.equalsIgnoreCase(qName)) {
            gapFrom = attributes.getValue(GAPFROM_ATTR);
        }
        if(PAGE_NODE.equalsIgnoreCase(qName)) {
            insidePage = true;
            pageId = Integer.parseInt(attributes.getValue(ID_ATTR));
            pageTitle = attributes.getValue(TITLE_ATTR);
        } else if(insidePage && REVISION_NODE.equalsIgnoreCase(qName)) {
            insideRevision = true;
            pageRev = Integer.parseInt(attributes.getValue(REV_ATTR));
            handler.startWikiPage(pageId, pageRev, pageTitle);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (insideRevision) {
            handler.wikiPageContent(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!insideRevision && insidePage && PAGE_NODE.equalsIgnoreCase(qName)) {
            insidePage = false;
            handler.endWikiPage();
        } else if (insideRevision && REVISION_NODE.equalsIgnoreCase(qName)) {
            insideRevision = false;
        }
    }

}
