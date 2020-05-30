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

/**
 * Set of <i>Wikipedia</i> related utility functions.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiPediaUtils {

    public static final String INFOBOX_TEMPLATE_NAME = "infobox";

    public static String getInfoBoxName(String templateName) {
        final String[] parts = templateName.split("\\s");
        return parts.length >= 2 && INFOBOX_TEMPLATE_NAME.equalsIgnoreCase(parts[0]) ? parts[0] : null;
    }

    private WikiPediaUtils() {}

}
