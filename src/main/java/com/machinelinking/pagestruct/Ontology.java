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

package com.machinelinking.pagestruct;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface Ontology {

    static final String TYPE_FIELD = "@type";

    static final String TYPE_ENRICHED_ENTITY = "entity";
    static final String TYPE_PAGE = "page";
    static final String TYPE_SECTION = "section";
    static final String TYPE_PARAGRAPH = "paragraph";
    static final String TYPE_REFERENCE = "reference";
    static final String TYPE_LINK = "link";
    static final String TYPE_LIST = "list";
    static final String TYPE_LIST_ITEM = "list_item";
    static final String TYPE_TEMPLATE = "template";
    static final String TYPE_VAR = "var";
    static final String TYPE_TABLE = "table";
    static final String TYPE_TABLE_HEAD_CELL = "head_cell";
    static final String TYPE_TABLE_BODY_CELL = "body_cell";
    static final String TYPE_OPEN_TAG = "open_tag";
    static final String TYPE_CLOSE_TAG = "close_tag";
    static final String TYPE_INLINE_TAG = "inline_tag";
    static final String TYPE_COMMENT_TAG = "comment_tag";
    static final String TYPE_ENTITY = "entity";
    static final String TYPE_MAPPING = "mapping";

    static final String PAGE_DOM_FIELD = "wikitext-dom";
    static final String STRUCTURE_FIELD = "structure";
    static final String ISSUES_FIELD = "issues";
    static final String ABSTRACT_FIELD = "abstract";
    static final String CATEGORIES_FIELD = "categories";
    static final String SECTIONS_FIELD = "sections";
    static final String SECTIONS_TEXT_FIELD = "sections_text";
    static final String REFERENCES_FIELD = "references";
    static final String LINKS_FIELD = "links";
    static final String FREEBASE_FIELD = "freebase";
    static final String TEMPLATES_FIELD = "templates";
    static final String TEMPLATE_MAPPING_FIELD = "template-mapping";

    static final String ID_FIELD = "id";
    static final String REVID_FIELD = "revid";
    static final String NAME_FIELD = "name";
    static final String TITLE_FIELD = "title";
    static final String URL_FIELD = "url";
    static final String SIZE_FIELD = "size";
    static final String LABEL_FIELD = "label";
    static final String LEVEL_FIELD = "level";
    static final String ITEM_TYPE_FIELD = "item_type";
    static final String ANCESTORS_FIELD = "ancestors";
    static final String CONTENT_FIELD = "content";
    static final String DEFAULT_FIELD = "default";

    static final String ANON_NAME_PREFIX = "@an";

}
