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

package com.machinelinking.util;

/**
 * Provides generic String utility methods.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class StringUtils {

    public static String md5bytesToHex(byte[] md5) {
        StringBuilder sb = new StringBuilder();
        for (byte aMd5 : md5) {
            sb.append(Integer.toString((aMd5 & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String stripTags(String in) {
        char c;
        int insideTag = 0;
        final StringBuilder out = new StringBuilder();
        for(int i = 0; i < in.length(); i++) {
            c = in.charAt(i);
            if(c == '<') {
                insideTag++;
            } else if(c == '>') {
                insideTag--;
            }
            else if(insideTag == 0) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private StringUtils() {}

}
