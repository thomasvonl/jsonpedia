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


import com.machinelinking.serializer.Serializable;
import com.machinelinking.serializer.Serializer;
import com.machinelinking.util.StringUtils;
import com.machinelinking.wikimedia.WikimediaUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Defines a <i>Wikipedia reference</i>.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Reference implements Serializable {

    public static final String[] IMAGE_EXT = new String[] {"jpg", "png"};

    private static final String IMAGE_PREFIX = "Image:";
    private static final String FILE_PREFIX = "File:";
    public static final String CATEGORY_PREFIX = "Category:";


    public static final int DEFAULT_IMAGE_WIDTH = 110;

    public static final String ANNOTATE_HTML_PREFIX = "/annotate/resource/html/";

    private URL url;
    private String description;
    private short sectionIndex;

    /**
     * Checks whether given URL describes an image.
     *
     * @param url
     * @return
     */
    public static boolean isImage(String url) {
        final String urlLower = url.toLowerCase();
        for(String ext : IMAGE_EXT) {
            if(urlLower.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    public static String getURLDeclaredLang(URL url) {
        return url.getHost().split("\\.")[0];
    }

    public static boolean isLangPrefix(String prefix) {
        return prefix.matches("[a-z]{2,3}(-[a-z]{1,3})?(-[a-z]{2,3})?");
    }

    public static String imageResourceToURL(String imgResource) {
        final int fileStart = imgResource.lastIndexOf('/') + 1;
        final String file = imgResource.substring(fileStart + FILE_PREFIX.length());
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        final String filename = file.replaceAll(" ", "_");
        messageDigest.update(filename.getBytes());
        final String digest = StringUtils.md5bytesToHex(messageDigest.digest());
        final String urlEncFile;
        final String folder;
        try {
            urlEncFile = URLEncoder.encode(filename, "utf-8");
            folder = String.format(
                    "%c/%c%c/%s/%dpx-%s",
                    digest.charAt(0),
                    digest.charAt(0), digest.charAt(1),
                    urlEncFile,
                    DEFAULT_IMAGE_WIDTH,
                    urlEncFile
            );
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee);
        }

        //final String location = folder.endsWith(".png") ? "en" : "commons";
        // http://upload.wikimedia.org/wikipedia/commons/f/fb/Albert_Einstein_at_the_age_of_three_%281882%29.jpg
        return String.format("http://upload.wikimedia.org/wikipedia/commons/thumb/%s", folder);
    }

    public static URL labelToURL(URL document, String label) throws MalformedURLException {
        final String normalizedLabel = label.replaceAll(" ", "_");
        final int prefixIndex = label.indexOf(":");
        final String documentString = document.toExternalForm();
        final String documentPrefix = documentString.substring(0, documentString.lastIndexOf('/') + 1);
        if(prefixIndex == -1) {
            return new URL(documentPrefix + normalizedLabel);
        } else {
            final String prefix = label.substring(0, prefixIndex + 1);
            switch (prefix) {
                case FILE_PREFIX:
                case IMAGE_PREFIX:
                case CATEGORY_PREFIX:
                    return new URL(WikimediaUtils.getEntityPath(document.toExternalForm()) + normalizedLabel);
                default:
                    if(prefixIndex == 0) { // es: ':United_Kingdom'
                        return new URL(toURLString(getURLDeclaredLang(document), normalizedLabel.substring(1)));
                    }
                    final String labelPrefix = normalizedLabel.substring(0, prefixIndex);
                    if(isLangPrefix(labelPrefix)) {
                        return new URL(toURLString(labelPrefix, normalizedLabel.substring(prefixIndex+1)));
                    } else {
                        return new URL(toURLString(getURLDeclaredLang(document), normalizedLabel));
                    }
            }
        }
    }

    public static String[] urlToLabel(String in) {
        final String HTTP_PREFIX = "http://";
        if(in.startsWith(HTTP_PREFIX)) {
            return new String[] {
                    in.substring(HTTP_PREFIX.length(), in.indexOf('.')),
                    in.substring(in.lastIndexOf('/') + 1)
            };
        } else {
            return in.split(":");
        }
    }

    public static String toURLString(String lang, String label) {
        return String.format("http://%s.wikipedia.org/wiki/%s", lang, label);
    }

    public static String toInternalURLString(String lang, String label) {
        return String.format("%s%s:%s", ANNOTATE_HTML_PREFIX, lang, label);
    }

    public static String toInternalURLString(String url) throws UnsupportedEncodingException {
        return ANNOTATE_HTML_PREFIX + URLEncoder.encode(url, "utf-8");
    }

    public Reference(URL document, String label, String description, short sectionIndex)
    throws MalformedURLException {
        this.url = labelToURL(document, label);
        this.description = description;
        this.sectionIndex = sectionIndex;
    }

    public URL getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public short getSectionIndex() {
        return sectionIndex;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.openObject();
        serializer.fieldValue("url", url.toExternalForm());
        serializer.fieldValue("description", description);
        serializer.fieldValue("section_idx", sectionIndex);
        serializer.closeObject();
    }

}
