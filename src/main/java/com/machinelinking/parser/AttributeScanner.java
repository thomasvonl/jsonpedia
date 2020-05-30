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

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a tolerant scanner to read key value attributes like:
 * <ul>
 * <lu><code>v1 &quotes;v 2&quotes; v3</code></li>
 * <lu><code>k1 = v1</code></li>
 * <lu><code>k1 = &quotes;v 1&quotes;</code></li>
 * <lu><code>v1 k2 = v2 k3 = &quotes;v 3&quotes; v4</code></li>
 * </ul>
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class AttributeScanner {

    public static final char DEFAULT_SEPARATOR_CHAR = ' ';
    public static final char DEFAULT_ASSIGN_CHAR = '=';
    public static final char DEFAULT_DELIMITER_CHAR = '"';

    public static Attribute[] scan(
            final char SEPARATOR, final char ASSIGN, final char DELIMITER, final String content
    ) {
        final StringBuilder buffer = new StringBuilder();
        final List<Attribute> attributes = new ArrayList<>();
        String lastBufferContent = null;
        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);
            if(c == SEPARATOR) {
                if(buffer.length() > 0) {
                    lastBufferContent = buffer.toString();
                    clearBuffer(buffer);
                }
            } else if(c == DELIMITER) {
                if(lastBufferContent != null) {
                    attributes.add(new Attribute(null, lastBufferContent));
                    lastBufferContent = null;
                }
                clearBuffer(buffer);
                i = scanValue(SEPARATOR, DELIMITER, content, i, buffer);
                if(buffer.length() > 0) {
                    lastBufferContent = buffer.toString();
                    buffer.delete(0, buffer.length());
                }
            } else if(c == ASSIGN) {
                final String k;
                if(lastBufferContent != null && buffer.length() > 0) {
                    attributes.add(new Attribute(null, lastBufferContent));
                    lastBufferContent = null;
                    k = buffer.toString();
                } else if(buffer.length() > 0) {
                    k = buffer.toString();
                } else if(lastBufferContent != null) {
                    k = lastBufferContent;
                    lastBufferContent = null;
                } else {
                    throw new IllegalStateException();
                }

                clearBuffer(buffer);
                i = scanValue(SEPARATOR, DELIMITER, content, i + 1, buffer);
                final String v = buffer.toString();
                clearBuffer(buffer);
                attributes.add(new Attribute(k, v));
            } else {
                buffer.append(c);
            }
        }
        if(lastBufferContent != null) {
            attributes.add(new Attribute(null, lastBufferContent));
        }
        if (buffer.length() > 0) {
            attributes.add(new Attribute(null, buffer.toString()));
        }
        return attributes.toArray(new Attribute[attributes.size()]);
    }

    public static Attribute[] scan(String content) {
        return scan(DEFAULT_SEPARATOR_CHAR, DEFAULT_ASSIGN_CHAR, DEFAULT_DELIMITER_CHAR, content);
    }

    // TODO: add escape support.
    protected static int scanValue(
            final char SEPARATOR, final char VALUE_DELIMITER,
            final String content, final int index, final StringBuilder out
    ) {
        boolean withinQuotes = false;
        char c;
        int i;
        for (i = index; i < content.length(); i++) {
            c = content.charAt(i);
            /*
            if(!withinQuotes && ASSIGN == c) throw new IllegalArgumentException(
                    String.format("Invalid char %c at position %d", ASSIGN, i)
            );
            */
            if (VALUE_DELIMITER == c) {
                if (withinQuotes) {
                    break;
                } else {
                    withinQuotes = true;
                }
            } else if (!withinQuotes && SEPARATOR == c) {
                if (out.length() > 0) {
                    return i;
                }
            } else {
                out.append(c);
            }
        }
        return i;
    }

    protected static int scanValue(final String content, final int index, final StringBuilder out) {
        return scanValue(
                DEFAULT_SEPARATOR_CHAR, DEFAULT_DELIMITER_CHAR, content, index, out
        );
    }

    private static final void clearBuffer(StringBuilder sb) {
        if(sb.length() > 0) sb.delete(0, sb.length());
    }

}
