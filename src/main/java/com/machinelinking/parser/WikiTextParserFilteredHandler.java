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

/**
 * Provides a filter decorator on events directed to a {@link com.machinelinking.parser.WikiTextParserHandler} based on
 * a configurable {@link com.machinelinking.parser.FilteredHandlerCriteria}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserFilteredHandler implements WikiTextParserHandler {

    private final WikiTextParserHandler decorated;
    private final FilteredHandlerCriteria criteriaHandler;

    private int paragraphIndex = 0;
    private int sectionLevel = -1;
    private int nestingLevel = 0;
    private boolean plainTextFound = false;

    public WikiTextParserFilteredHandler(WikiTextParserHandler decorated, FilteredHandlerCriteria criteriaHandler) {
        this.decorated = decorated;
        this.criteriaHandler = criteriaHandler;
    }

    @Override
    public void beginDocument(URL document) {
        decorated.beginDocument(document);
    }

    @Override
    public void paragraph() {
        paragraphIndex++;
        if(mustFilter()) return;
        decorated.paragraph();
    }

    @Override
    public void section(String title, int level) {
        sectionLevel = level;
        if(mustFilter()) return;
        decorated.section(title, level);
    }

    @Override
    public void parseWarning(String msg, ParserLocation location) {
        if(mustFilter()) return;
        decorated.parseWarning(msg, location);
    }

    @Override
    public void parseError(Exception e, ParserLocation location) {
        if(mustFilter()) return;
        decorated.parseError(e, location);
    }

    @Override
    public void beginReference(String label) {
        nestingLevel++;
        if(mustFilter()) return;
        decorated.beginReference(label);
    }

    @Override
    public void endReference(String label) {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endReference(label);
    }

    @Override
    public void beginLink(URL url) {
        nestingLevel++;
        if(mustFilter()) return;
         decorated.beginLink(url);
    }

    @Override
    public void endLink(URL url) {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endLink(url);
    }

    @Override
    public void beginList() {
        nestingLevel++;
        if(mustFilter()) return;
        decorated.beginList();
    }

    @Override
    public void listItem(ListType t, int level) {
        if(mustFilter()) return;
        decorated.listItem(t, level);
    }

    @Override
    public void endList() {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endList();
    }

    @Override
    public void beginTemplate(TemplateName name) {
        nestingLevel++;
        if(mustFilter()) return;
        decorated.beginTemplate(name);
    }

    @Override
    public void endTemplate(TemplateName name) {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endTemplate(name);
    }

    @Override
    public void beginTable() {
        nestingLevel++;
        if(mustFilter()) return;
        decorated.beginTable();
    }

    @Override
    public void headCell(int row, int col) {
        if(mustFilter()) return;
        decorated.headCell(row, col);
    }

    @Override
    public void bodyCell(int row, int col) {
        if(mustFilter()) return;
        decorated.bodyCell(row, col);
    }

    @Override
    public void endTable() {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endTable();
    }

    @Override
    public void parameter(String param) {
        if(mustFilter()) return;
        decorated.parameter(param);
    }

    @Override
    public void entity(String form, char value) {
        if(mustFilter()) return;
        decorated.entity(form, value);
    }

    @Override
    public void var(Var var) {
        if(mustFilter()) return;
        decorated.var(var);
    }

    @Override
    public void text(String content) {
        if(!plainTextFound && nestingLevel == 0) plainTextFound = true;
        if(mustFilter()) return;
        decorated.text(content);
    }

    @Override
    public void italicBold(int level) {
        if(mustFilter()) return;
        decorated.italicBold(level);
    }

    @Override
    public void endDocument() {
        decorated.endDocument();
    }

    @Override
    public void beginTag(String node, Attribute[] attributes) {
        nestingLevel++;
        if(mustFilter()) return;
        decorated.beginTag(node, attributes);
    }

    @Override
    public void endTag(String node) {
        nestingLevel--;
        if(mustFilter()) return;
        decorated.endTag(node);
    }

    @Override
    public void inlineTag(String node, Attribute[] attributes) {
        if(mustFilter()) return;
        decorated.inlineTag(node, attributes);
    }

    @Override
    public void commentTag(String comment) {
        if(mustFilter()) return;
        decorated.commentTag(comment);
    }

    protected boolean mustFilter() {
        return criteriaHandler.mustFilter(paragraphIndex, sectionLevel, nestingLevel, plainTextFound);
    }

}
