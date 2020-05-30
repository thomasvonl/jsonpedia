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
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class EntityExpansionReader extends Reader {

    private static final short MAX_ENTITY_NUMERIC_SIZE = 6;

    private final BufferedReader inner;

    private final StringBuilder entityContentBuffer = new StringBuilder();

    private CharBuffer charBuffer = null;

    public static void readEntity(ParserReader reader, WikiTextParserHandler handler) throws IOException {
        char c;
        c = reader.read();
        if(c != '&') throw new IllegalStateException();

        final StringBuilder entityContentBuffer = new StringBuilder();
        boolean foundTerminal = false;
        for (int l = 0; l < MAX_ENTITY_NUMERIC_SIZE; l++) {
            c = reader.read();
            if (c == ';') {
                foundTerminal = true;
                break;
            }
            if (invalidChar(c)) {
                entityContentBuffer.append(c);
                break;
            }
            entityContentBuffer.append(c);
        }
        final String contentBuffer = entityContentBuffer.toString();
        if (foundTerminal) {
            final char entityChar = convertToChar(contentBuffer);
            handler.entity(contentBuffer, entityChar);
        } else {
            handler.text("&" + contentBuffer);
        }
        reader.mark();
    }

    public static char entityNameToChar(String name) {
        // TODO: complete.
        switch (name) {
            case "lt":
                return '<';
            case "gt":
                return '>';
            case "nbsp":
                return ' ';
            case "mdash":
                return '—';
            case "ndash":
                return '–';
            default:
                //throw new IllegalArgumentException("Unknown entity name: " + name);
                return '?';
        }
    }

    public static Character convertToChar(String entitySequence) {
        final char type = entitySequence.charAt(0);
        if (type == '#') { // &#nnnn; OR &#xhhhh;
            if (entitySequence.length() > 1) {
                char result;
                if (entitySequence.charAt(1) == 'x') {
                    result = (char) Long.parseLong(entitySequence.substring(2), 16);
                } else {
                    result = (char) Integer.parseInt(entitySequence.substring(1));
                }
                return result;
            } else {
                return null;
            }
        } else { // &name;
            return entityNameToChar(entitySequence);
        }
    }

    private static boolean invalidChar(char c) {
        return c != '#' && (c < 'a' || c > 'z') && (c  < 'A' || c > 'Z') && (c < '0' || c > '9');
    }

    public EntityExpansionReader(BufferedReader inner) {
        this.inner = inner;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        inner.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        inner.reset();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int intc;
        char c;
        int writtenChars = 0;
        boolean foundTerminal = false;
        for (int i = off; i < len; i++) {
            intc = internalRead();
            if (intc == -1) break;
            c = (char) intc;
            if (c == '&') {
                entityContentBuffer.delete(0, entityContentBuffer.length());
                for(int l = 0; l < MAX_ENTITY_NUMERIC_SIZE; l++) {
                    intc = inner.read();
                    if (intc == -1) break;
                    c = (char) intc;
                    if (c == ';') {
                        foundTerminal = true;
                        break;
                    }
                    if (invalidChar(c)) {
                        entityContentBuffer.append(c);
                        break;
                    }
                    entityContentBuffer.append(c);
                }
                final String entityContent = entityContentBuffer.toString();
                if(foundTerminal && entityContent.length() > 0) {
                    final Character entity = convertToChar(entityContent);
                    if(entity != null) {
                        cbuf[i] = entity;
                        writtenChars++;
                    } else { // One char more to read.
                        i--;
                    }
                } else { // &something incomplete.
                    cbuf[i++] = '&';
                    writtenChars++;
                    final int flushed = flushToBuffer(cbuf, i, entityContent);
                    final String delta = entityContent.substring(flushed);
                    if(delta.length() > 0) internalWrite(delta);
                    i += flushed - 1; // to compensate the final increment.
                    writtenChars += flushed;
                }
            } else {
                cbuf[i] = c;
                writtenChars++;
            }
        }
        return writtenChars == 0 ? -1 : writtenChars;
    }

    @Override
    public void close() throws IOException {
        this.inner.close();
    }

    private int internalRead() throws IOException {
        if(charBuffer != null && charBuffer.hasRemaining()) {
            return charBuffer.get();
        } else {
            return inner.read();
        }
    }

    private void internalWrite(String content) {
        if(charBuffer == null) {
            charBuffer = CharBuffer.wrap(content);
        } else {
            charBuffer = CharBuffer.wrap(charBuffer.toString() + new String(content.toCharArray())); // TODO: Remove usage of charbuffer
        }
    }

    private int flushToBuffer(char[] buffer, int start, CharSequence sequence) {
        int i;
        final int limit = Math.min(buffer.length, start + sequence.length());
        for(i = start; i < limit; i++)
            buffer[i] = sequence.charAt(i - start);
        return i - start;
    }

}
