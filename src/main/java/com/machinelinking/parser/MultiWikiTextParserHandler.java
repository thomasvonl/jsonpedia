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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to multiply {@link WikiTextParserHandler}s messages.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiWikiTextParserHandler implements WikiTextParserHandler {

    private List<WikiTextParserHandler> handlers = new ArrayList<WikiTextParserHandler>();

    public MultiWikiTextParserHandler() {}

    public void add(WikiTextParserHandler h) {
        handlers.add(h);
    }

    public void remove(WikiTextParserHandler h) {
        handlers.remove(h);
    }

    @Override
    public void beginDocument(URL document) {
        for(WikiTextParserHandler handler : handlers) {
            handler.beginDocument(document);
        }
    }

    @Override
    public void parseWarning(String msg, ParserLocation location) {
        for(WikiTextParserHandler handler : handlers) {
            handler.parseWarning(msg, location);
        }
    }

    @Override
    public void parseError(Exception e, ParserLocation location) {
        for(WikiTextParserHandler handler : handlers) {
            handler.parseError(e, location);
        }
    }

    @Override
    public void paragraph() {
        for(WikiTextParserHandler handler : handlers) {
            handler.paragraph();
        }
    }

    @Override
    public void section(String title, int level) {
        for(WikiTextParserHandler handler : handlers) {
            handler.section(title, level);
        }
    }

    @Override
    public void beginReference(String label) {
        for (WikiTextParserHandler handler : handlers) {
            handler.beginReference(label);
        }
    }

    @Override
    public void endReference(String label) {
        for (WikiTextParserHandler handler : handlers) {
            handler.endReference(label);
        }
    }

    @Override
    public void beginLink(URL url) {
        for (WikiTextParserHandler handler : handlers) {
            handler.beginLink(url);
        }
    }

    @Override
    public void endLink(URL url) {
        for (WikiTextParserHandler handler : handlers) {
            handler.endLink(url);
        }
    }

    @Override
    public void beginList() {
        for(WikiTextParserHandler handler : handlers) {
            handler.beginList();
        }
    }

    @Override
    public void listItem(ListType t, int level) {
        for(WikiTextParserHandler handler : handlers) {
            handler.listItem(t, level);
        }
    }

    @Override
    public void endList() {
        for(WikiTextParserHandler handler : handlers) {
            handler.endList();
        }
    }

    @Override
    public void beginTemplate(TemplateName name) {
        for(WikiTextParserHandler handler : handlers) {
            handler.beginTemplate(name);
        }
    }

    @Override
    public void endTemplate(TemplateName name) {
        for(WikiTextParserHandler handler : handlers) {
            handler.endTemplate(name);
        }
    }

    @Override
    public void beginTable() {
        for(WikiTextParserHandler handler : handlers) {
            handler.beginTable();
        }
    }

    @Override
    public void headCell(int row, int col) {
        for(WikiTextParserHandler handler : handlers) {
            handler.headCell(row, col);
        }
    }

    @Override
    public void bodyCell(int row, int col) {
        for(WikiTextParserHandler handler : handlers) {
            handler.bodyCell(row, col);
        }
    }

    @Override
    public void endTable() {
        for(WikiTextParserHandler handler : handlers) {
            handler.endTable();
        }
    }

    @Override
    public void beginTag(String node, Attribute[] attributes) {
        for(WikiTextParserHandler handler : handlers) {
            handler.beginTag(node, attributes);
        }
    }

    @Override
    public void endTag(String node) {
        for(WikiTextParserHandler handler : handlers) {
            handler.endTag(node);
        }
    }

    @Override
    public void inlineTag(String node, Attribute[] attributes) {
        for(WikiTextParserHandler handler : handlers) {
            handler.inlineTag(node, attributes);
        }
    }

    @Override
    public void commentTag(String comment) {
        for(WikiTextParserHandler handler : handlers) {
            handler.commentTag(comment);
        }
    }

    @Override
    public void parameter(String param) {
        for(WikiTextParserHandler handler : handlers) {
            handler.parameter(param);
        }
    }

    @Override
    public void entity(String form, char value) {
        for(WikiTextParserHandler handler : handlers) {
            handler.entity(form, value);
        }
    }

    @Override
    public void var(Var v) {
        for(WikiTextParserHandler handler : handlers) {
            handler.var(v);
        }
    }

    @Override
    public void text(String content) {
        for(WikiTextParserHandler handler : handlers) {
            handler.text(content);
        }
    }

    @Override
    public void italicBold(int level) {
        for (WikiTextParserHandler handler : handlers) {
            handler.italicBold(level);
        }
    }

    @Override
    public void endDocument() {
        for(WikiTextParserHandler handler : handlers) {
            handler.endDocument();
        }
    }

}
