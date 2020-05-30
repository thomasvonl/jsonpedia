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

package com.machinelinking.wikimedia;

/**
 * Models a single <i>Wikipage</i>
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiPage {

    private final int id;
    private final int revId;
    private final String title;
    private final String content;

    public WikiPage(int id, int revid, String title, String content) {
        this.id      = id;
        this.revId = revid;
        this.title   = title;
        this.content = content;
    }

    public int getId() {
         return id;
    }

    public int getRevId() {
         return revId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getSize() {
        return title.length() + content.length() + 2;
    }

    @Override
    public String toString() {
        return String.format("id: %d title: %s\n\n content:%s\n", id, title, content);
    }

}
