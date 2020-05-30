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
 * Default empty {@link WikiTextParserHandler} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultWikiTextParserHandler implements WikiTextParserHandler {

    @Override
    public void beginDocument(URL document) {
    }

    @Override
    public void paragraph() {
    }

    @Override
    public void parseWarning(String msg, ParserLocation location) {
    }

    @Override
    public void parseError(Exception e, ParserLocation location) {
    }

    @Override
    public void section(String title, int level) {
    }

    @Override
    public void beginReference(String label) {
    }

    @Override
    public void endReference(String label) {
    }

    @Override
    public void beginLink(URL url) {
    }

    @Override
    public void endLink(URL url) {
    }

    @Override
    public void beginList() {
    }

    @Override
    public void listItem(ListType t, int level) {
    }

    @Override
    public void endList() {
    }

    @Override
    public void beginTemplate(TemplateName name) {
    }

    @Override
    public void endTemplate(TemplateName name) {
    }

    @Override
    public void beginTable() {
    }

    @Override
    public void headCell(int row, int col) {
    }

    @Override
    public void bodyCell(int row, int col) {
    }

    @Override
    public void endTable() {
    }

    @Override
    public void beginTag(String name, Attribute[] attributes) {
    }

    @Override
    public void endTag(String name) {
    }

    @Override
    public void inlineTag(String name, Attribute[] attributes) {
    }

    @Override
    public void commentTag(String comment) {
    }

    @Override
    public void parameter(String param) {
    }

    @Override
    public void entity(String form, char value) {
    }

    @Override
    public void var(Var v) {
    }

    @Override
    public void text(String content) {
    }

    @Override
    public void italicBold(int level) {
    }

    @Override
    public void endDocument() {
    }

}
