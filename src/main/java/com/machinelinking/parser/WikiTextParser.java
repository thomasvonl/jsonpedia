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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The <i>Wikitext</i> event parser. <b>This class needs a complete rewriting.</b>
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
// TODO HIGH: error detection is not working, see BrokenTemplate1 wikitext
// TODO: complete support for ordered lists
public class WikiTextParser implements ParserReader {

    protected static final String MATH_NODE   = "math";
    protected static final String NOWIKI_NODE = "nowiki";
    protected static final String[] UNPARSED_NODES = new String[] {MATH_NODE, NOWIKI_NODE};

    private enum InternalListType {
        Unordered {
            @Override
            char getTrailingChar() {
                return '*';
            }
            @Override
            WikiTextParserHandler.ListType getType() {
                return WikiTextParserHandler.ListType.Unordered;
            }
        },
        Numbered {
            @Override
            char getTrailingChar() {
                return '#';
            }
            @Override
            WikiTextParserHandler.ListType getType() {
                return WikiTextParserHandler.ListType.Numbered;
            }
        };

        abstract char getTrailingChar();
        abstract WikiTextParserHandler.ListType getType();
    }

    private static final String[] TEMPLATE_CLOSURE = new String[]{"}}", "|"};

    private static final String[] TEMPLATE_LIST_DELIMITER = new String[]{"\n", "}}", "|", "]]", "]"};

    private static final String[] TABLE_DELIMITERS = new String[]{"|}", "|-", "!!" , "!", "||", "|"};

    private static final String[] REFERENCE_DELIMITERS = new String[] {"]]", "]", "\n", "|"};

    private static final String[] LINK_DELIMITERS  = new String[] {"]",  "<"};

    private static final int AHEAD = 500;

    private Reader r;

    private int row, markrow, col, markcol;

    private final TagReader tagReader;

    private WikiTextParserHandler handler;

    public WikiTextParser(WikiTextParserHandler h) {
        if(h == null) throw new NullPointerException("Handler must be not null.");
        handler = h;
        tagReader = new TagReader(handler);
    }

    public void parse(URL url, Reader r) throws IOException, WikiTextParserException {
        BufferedReader br;
        if(!(r instanceof BufferedReader)) {
            br = new BufferedReader(r);
        } else {
            br = (BufferedReader) r;
        }
        this.r = br;
        row = markrow = 1; col = markcol = 1;

        handler.beginDocument(url);

        try {
            while(true) {
                consumeChars();
                mark();
                final String couple = readCouple();
                /*if("\n\n".equals(couple)) {
                    //mark();
                    handler.paragraph();
                } else */if('&' == couple.charAt(0)) {
                    reset();
                    EntityExpansionReader.readEntity(this, handler);
                } else if('<' == couple.charAt(0)) {
                    reset();
                    tagReader.readNode(this);
                    mark();
                    if (tagReader.isInsideNode(UNPARSED_NODES)) {
                        tagReader.readUntilNextTag(this);
                    }
                } else if ("{{".equals(couple)) {
                    readTemplate();
                } else if ("[[".equals(couple)) {
                    readReference();
                } else if('[' == couple.charAt(0)) {
                    reset(); read(); mark(); // TODO improve it.
                    readLink();
                } else if("{|".equals(couple)) {
                    readTable();
                } else if(col == 2 && "==".equals(couple)) {
                    readSection();
                } else if('=' == couple.charAt(0)) {
                    reset(); read(); mark(); // TODO improve it.
                } else if(col == 2 && '*' == couple.charAt(0)) {
                    reset();
                    readList(InternalListType.Unordered);
                } else if(col == 2 && '#' == couple.charAt(0)) {
                    reset();
                    readList(InternalListType.Numbered);
                }
            }
        } catch (EOFException eofe) {
            // Parse ended.
        } catch (Exception e) {
            e.printStackTrace();
            final WikiTextParserException wtpe = new WikiTextParserException(
                    row, col,
                    "Error while parsing document.", e
            );
            handler.parseError(wtpe, new DefaultParserLocation(this.row, this.col) );
            throw wtpe;
        } finally {
            handler.endDocument();
        }
    }

    public void parse(URL url, InputStream is) throws IOException, WikiTextParserException {
        parse(url, new InputStreamReader(is));
    }

    public void parse(DocumentSource source) throws IOException, WikiTextParserException {
        parse(
                source.getDocumentURL(),
                source.getInputStream()
        );
    }

    public char read() throws IOException {
        int intc = r.read();
        if(intc == -1) throw new EOFException();
        char c = (char) intc;
        if(c == '\n') {
            col = 0;
            row++;
        } else {
            col++;
        }
        return c;
    }

    public void mark() throws IOException {
        r.mark(AHEAD);
        markrow = row;
        markcol = col;
    }

    public void reset() throws IOException {
        if(r != null) r.reset();
        row = markrow;
        col = markcol;
    }

    @Override
    public ParserLocation getLocation() {
        return new DefaultParserLocation(this.row, this.col);
    }

    private boolean assertChar(char c) throws IOException {
        return c == read();
    }

    private void assertCharOrFail(char c) throws IOException {
        final char curr = read();
        if(curr != c) throw new IllegalStateException(String.format("Expected: %s, found %s", c, curr));
    }

    final char[] couple = new char[2];
    private String readCouple() throws IOException {
        couple[0] = read();
        couple[1] = read();
        return new String(couple);
    }

    private void flushText(StringBuilder sb) {
        if (sb.length() > 0) {
            handler.text( sb.toString() );
            sb.delete(0, sb.length());
        }
    }

    private void clear(StringBuilder sb) {
        sb.delete(0, sb.length());
    }

    private String flushAndClear(StringBuilder sb) {
        final String content = sb.toString();
        clear(sb);
        return content;
    }

    private void consumeSpaces() throws IOException {
        mark();
        char c;
        while(true) {
            c = read();
            if(c == '\n' || c == '\t' || c == ' ') {
                mark();
            } else {
                reset();
                break;
            }
        }
    }

    private final StringBuilder externalCharsSB = new StringBuilder();
    private void consumeChars() throws IOException {
        mark();
        clear(externalCharsSB);
        char c;
        try {
            while (true) {
                c = read();
                if(c == '\'') { // Manage ' sequences
                    mark();
                    int apostrophesCount = 1;
                    try {
                        while (true) {
                            c = read();
                            if (c != '\'') {
                                reset();
                                break;
                            }
                            apostrophesCount++;
                            mark();
                        }
                    } finally {
                        if (apostrophesCount >= 2) {
                            flushText(externalCharsSB);
                            handler.italicBold(apostrophesCount);
                        } else {
                            externalCharsSB.append('\'');
                        }
                    }
                } else if(c == '\n') { // Handling paragraph.
                        mark();
                    try {
                        c = read();
                        if (c == '\n') {
                            mark();
                            flushText(externalCharsSB);
                            handler.paragraph();
                        } else {
                            externalCharsSB.append('\n');
                            reset();
                        }
                    } catch (EOFException eof) {
                        externalCharsSB.append('\n');
                    }
                } else if (
                        c != '{' &&
                        c != '[' &&
                        c != '<' &&
                        c != '&' &&
                        (c != '=' || col > 3) &&
                        (c != '*' || col > 1) &&
                        (c != '#' || col > 1)
                ) {
                    mark();
                    externalCharsSB.append(c);
                } else {
                    reset();
                    break;
                }
            }
        } finally {
            flushText(externalCharsSB);
        }
    }

    private final StringBuilder templateHeaderSB = new StringBuilder();
    private TemplateHeader readTemplateHeader() throws IOException {
        clear(templateHeaderSB);
        char c;
        WikiTextParserHandler.Var var;
        final List<WikiTextParserHandler.Value> fragments = new ArrayList<>();
        while(true) {
            mark();
            c = read();
            if (c == '{') {
                reset();
                if(templateHeaderSB.length() > 0)
                    fragments.add(new WikiTextParserHandler.Const(flushAndClear(templateHeaderSB)));
                var = readVariableOrTemplate(false);
                if(var != null) fragments.add(var);
            } else if(c != '|' && c != '}') {
                templateHeaderSB.append(c);
            } else {
                if(c == '}')
                    reset();
                break;
            }
        }
        fragments.add(new WikiTextParserHandler.Const(flushAndClear(templateHeaderSB)));
        return new TemplateHeader(fragments.toArray(new WikiTextParserHandler.Value[fragments.size()]));
    }

    private final StringBuilder tableHeaderSB = new StringBuilder();
    private String readTableHeader() throws IOException {
        clear(tableHeaderSB);
        char c;
        while(true) {
            mark();
            c = read();
            if(c != '|') {
                tableHeaderSB.append(c);
            } else {
                mark();
                c = read();
                if(c == '-')
                    mark();
                else
                    reset();
                break;
            }
        }
        return tableHeaderSB.toString();
    }

    private WikiTextParserHandler.Var readVariableContent() throws IOException {
        char c;
        String name = null;
        final StringBuilder varSB = new StringBuilder();
        WikiTextParserHandler.Value defaultValue = null;
        while(true) {
            mark();
            c = read();
            if(c == '}') {
                if(name == null) name = varSB.toString();
                reset();
                break;
            } else if(c == '|') {
                name = varSB.toString();
                defaultValue = readDefaultValue();
            } else {
                varSB.append(c);
            }
        }
        return new WikiTextParserHandler.Var(
                name, defaultValue
        );
    }

    private final StringBuilder valueSB = new StringBuilder();
    private WikiTextParserHandler.Value readDefaultValue() throws IOException {
        char c;
        mark();
        c = read();
        if (c == '{') { // default is var.
            assertCharOrFail('{');
            mark();
            c = read();
            WikiTextParserHandler.Var defaultValue = null;
            if( c == '{') {
                //assertCharOrFail('{');
                defaultValue = readVariableContent();
                assertCharOrFail('}');
                assertCharOrFail('}');
                assertCharOrFail('}');
            } else {
                reset();
                readTemplate();
            }
            mark();
            return defaultValue;
        } else {
            reset();
            clear(valueSB);
            while(true) {
                mark();
                c = read();
                if(c == '}') {
                    reset();
                    break;
                }
                valueSB.append(c);
            }
            return new WikiTextParserHandler.Const(valueSB.toString());
        }
    }

    private WikiTextParserHandler.Var readVariableOrTemplate(boolean alreadyAsserted) throws IOException {
        if (!alreadyAsserted) {
            assertCharOrFail('{');
            assertCharOrFail('{');
        }
        mark();
        char c = read();
        WikiTextParserHandler.Var var = null;
        if(c == '{') { // Var.
            var = readVariableContent();
            assertCharOrFail('}');
            assertCharOrFail('}');
            assertCharOrFail('}');
        } else { // Template.
            reset();
            readTemplate();
        }
        mark();
        return var;
    }

    private int readPropertyValue(String[] lookAhead, boolean resetSequence, boolean produceNullParamKey, boolean ignoreAssignment, boolean listAllowed)
    throws IOException {
        final StringBuilder sb = new StringBuilder();
        char c;
        boolean foundAssignment = false;
        while(true) {
            mark();
            final int seq = lookAhead(lookAhead);
            if(seq != -1) {
                if(resetSequence) reset();
                if(produceNullParamKey && !foundAssignment && sb.length() > 0) {
                    handler.parameter(null);
                }
                flushText(sb);
                return seq;
            }

            c = read();

            if(!ignoreAssignment && !foundAssignment && c == '=') {
                foundAssignment = true;
                handler.parameter(sb.toString());
                clear(sb);
                mark();
                consumeSpaces();
                continue;
            }

            if(c == '<') {
                if (produceNullParamKey && !foundAssignment && sb.length() > 0) {
                    foundAssignment = true;
                    handler.parameter(null);
                }
                flushText(sb);
                reset();
                tagReader.readNode(this);
                mark();
                if (tagReader.isInsideNode(UNPARSED_NODES)) {
                    tagReader.readUntilNextTag(this);
                }
                continue;
            }

            // Nested element.
            if(c == '{') {
                mark();

                if(assertChar('{')) { // Nested template or variable.
                    mark();
                    if (produceNullParamKey && !foundAssignment) {
                        foundAssignment = true;
                        handler.parameter(null);
                    }
                    flushText(sb);

                    final WikiTextParserHandler.Var var = readVariableOrTemplate(true);
                    if(var != null) handler.var(var);
                    continue;
                } else {
                    reset();
                }

                if (assertChar('|')) { // Nested table.
                    mark();
                    if (produceNullParamKey && !foundAssignment) {
                        foundAssignment = true;
                        handler.parameter(null);
                    }
                    flushText(sb);
                    readTable();
                    continue;
                } else {
                    reset();
                }
            }

            if(c == '[') {
                mark();
                if(assertChar('[')) {
                    mark();
                    if (produceNullParamKey && !foundAssignment) {
                        foundAssignment = true;
                        handler.parameter(null);
                    }
                    flushText(sb);
                    readReference();
                    continue;
                } else {
                    reset();
                    if (produceNullParamKey && !foundAssignment) {
                        foundAssignment = true;
                        handler.parameter(null);
                    }
                    flushText(sb);
                    readLink();
                    continue;
                }
            }

            if(listAllowed && c == '*') {
                reset();
                readList(InternalListType.Unordered);
                continue;
            }

//            if(listAllowed && c == '#') {
//                reset();
//                readList(InternalListType.Unordered);
//                continue;
//            }

            sb.append(c);
        }
    }

    private void readTemplateProperties() throws IOException {
        while(true) {
            consumeSpaces();
            final int seq = readPropertyValue(TEMPLATE_CLOSURE, false, true, false, true);
            mark();
            if(seq == 0) break;
        }
    }

    private void readTemplate() throws IOException {
        final TemplateHeader templateHeader = readTemplateHeader();
        final WikiTextParserHandler.TemplateName templateName =
                new WikiTextParserHandler.TemplateName(templateHeader.fragments);

        handler.beginTemplate(templateName);

        consumeSpaces();
        mark();

        readTemplateProperties();
        mark();

        handler.endTemplate(templateName);
    }

    private final StringBuilder referenceLabelSB = new StringBuilder();
    private String readReferenceLabel() throws IOException {
        clear(referenceLabelSB);
        char c;
        while(true) {
            mark();
            c = read();
            if(c == '{') {
                reset();
                readVariableOrTemplate(false);
            }
            if(c != '|' && c != ']') {
                referenceLabelSB.append(c);
            } else {
                if(c == ']') reset();
                break;
            }
        }
        return referenceLabelSB.toString();
    }

    private void readReference() throws IOException {
        final String referenceLabel = readReferenceLabel();
        handler.beginReference(referenceLabel);
        int ahead;
        try {
            while (true) {
                ahead = readPropertyValue(REFERENCE_DELIMITERS, false, true, false, false);
                if (ahead == 0)
                    break;
                if (ahead == 1 || ahead == 2) { // TODO: this should be: } else {
                    handler.parseWarning("Invalid closure for reference.", getLocation());
                    break;
                }
            }
        } finally {
            handler.endReference(referenceLabel);
        }
    }

    private final StringBuilder linkURLSB = new StringBuilder();
    private LinkURL readLinkURL() throws IOException {
        clear(linkURLSB);
        char c;
        while(true) {
            mark();
            c = read();
            if(c == '{') {
                reset();
                try {
                    final WikiTextParserHandler.Var var = readVariableOrTemplate(false);
                    return var == null ? null : new VarLinkURL(var);
                } catch (IllegalStateException ise) {
                    read();
                    mark();
                }
            }
            if(c == LINK_DELIMITERS[0].charAt(0) || c == LINK_DELIMITERS[1].charAt(0)) {
                reset();
                break;
            }
            if(c != ' ' && c != '\t') {
                linkURLSB.append(c);
            } else {
                mark();
                break;
            }
        }
        return new StringLinkURL(linkURLSB.toString());
    }

    private void readLink() throws IOException {
        final LinkURL linkURL = readLinkURL();
        URL url = null;
        if(linkURL != null) {
            if(linkURL instanceof VarLinkURL) {
                handler.beginLink(null);
                handler.var(((VarLinkURL) linkURL).var);
            } else if(linkURL instanceof StringLinkURL) {
                final String urlString = ((StringLinkURL) linkURL).url;
                try {
                    url = new URL(urlString);
                    handler.beginLink(url);
                } catch (MalformedURLException murle) {
                    handler.text(String.format("[%s", urlString));
                    return;
                }
            }
        } else {
            handler.beginLink(null);
        }

        int ahead;
        while(true) {
            ahead = readPropertyValue(LINK_DELIMITERS, true, true, false, false);
            if(ahead == 0) { // TODO: improve this solution.
                read();
                mark();
            }else if(ahead == 1) {
                handler.parseWarning("Invalid link closure.", getLocation());
            }
            break;
        }
        handler.endLink(url);
    }

    private void readTable() throws IOException {
        handler.beginTable();
        mark();
        int ahead;
        int tableRow, tableCol;
        tableRow = tableCol = 1;
        final String header = readTableHeader();
        handler.text(header);
        handler.headCell(tableRow, tableCol);
        tableCol++;
        consumeSpaces();
        while(true) {
            ahead = readPropertyValue(TABLE_DELIMITERS, false, false, false, true);
            if(ahead == 0) {
                mark();
                handler.endTable();
                break;
            } else if(ahead == 3) {
                // Do nothing.
            } else if(ahead == 2) {
                mark();
                handler.headCell(tableRow, tableCol);
                tableCol++;
            } else if (ahead == 1) {
                mark();
                tableCol = 1;
                tableRow++;
            } else if(ahead == 4 || ahead == 5) {
                mark();
                tableCol++;
                handler.bodyCell(tableRow, tableCol);
            }
            consumeSpaces();
        }
    }

    private final StringBuilder sectionSB = new StringBuilder();
    private void readSection() throws IOException {
        mark();
        char c;

        // Consumes section begin.
        int sectionLevel = 0;
        while(true) {
            c = read();
            if(c != '=') {
                reset();
                break;
            } else {
                mark();
                sectionLevel++;
            }
        }

        // Read section content.
        clear(sectionSB);
        while(true) {
            c = read();
            if(c == '=' && assertChar('=')) {
                mark();
                break;
            } else{
                mark();
                sectionSB.append(c);
            }
        }

        // Consumes section end.
        readSequence('=');

        handler.section(sectionSB.toString(), sectionLevel);
    }

    private void readList(InternalListType type) throws IOException {
        handler.beginList();
        try {
            while (true) {
                int level = readSequence(type.getTrailingChar());
                if(level == 0) break;
                handler.listItem(type.getType(), level);
                final int delimiter = readPropertyValue(TEMPLATE_LIST_DELIMITER, true, false, true, true);
                if(delimiter == 0) {
                    read(); mark();
                }
            }
        } catch (EOFException e) {
            // Pass
        }
        handler.endList();
    }

    private int readSequence(char t) throws IOException {
        int count = 0;
        char c;
        while (true) {
            c = read();
            if (c != t) {
                reset();
                break;
            } else {
                mark();
                count++;
            }
        }
        return count;
    }

    private int lookAhead(String[] sequences) throws IOException {
        char c;
        boolean sequenceMatchComplete;
        for(int i = 0; i < sequences.length; i++) {
            sequenceMatchComplete = true;
            for(int k = 0; k < sequences[i].length(); k++) {
                c = read();
                if(c != sequences[i].charAt(k)) {
                    sequenceMatchComplete = false;
                    reset();
                    break;
                }
            }
            if(sequenceMatchComplete) {
                return i;
            }
        }
        return -1;
    }

    class TemplateHeader {
        final WikiTextParserHandler.Value[] fragments;
        TemplateHeader(final WikiTextParserHandler.Value[] fragments) {
            this.fragments = fragments;
        }
    }

    interface LinkURL {}

    class StringLinkURL implements LinkURL {
        final String url;
        StringLinkURL(String url) {
            this.url = url;
        }
    }

    class VarLinkURL implements LinkURL {
        final WikiTextParserHandler.Var var;
        VarLinkURL(WikiTextParserHandler.Var var) {
            this.var = var;
        }
    }

}
