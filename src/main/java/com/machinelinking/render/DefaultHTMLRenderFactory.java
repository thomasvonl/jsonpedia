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

package com.machinelinking.render;

/**
 * Default implementation of {@link HTMLRenderFactory}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultHTMLRenderFactory implements HTMLRenderFactory {

    private static DefaultHTMLRenderFactory instance = new DefaultHTMLRenderFactory();

    public static DefaultHTMLRenderFactory getInstance() {
        return instance;
    }

    public DefaultHTMLRender createRender(boolean alwaysRenderDefault) {
        final DefaultHTMLRender render = new DefaultHTMLRender(alwaysRenderDefault);
        // Root level.
        render.addKeyValueRender("issues"  , new IssuesKeyValueRender());
        render.addKeyValueRender("abstract", new AbstractKeyValueRender());
        render.addKeyValueRender("sections", new SectionsKeyValueRender());
        render.addKeyValueRender("links"   , new LinksKeyValueRender());
        render.addKeyValueRender("references", new ReferencesKeyValueRender());
        render.addKeyValueRender("templates", new TemplatesKeyValueRender());
        render.addKeyValueRender("categories", new CategoriesKeyValueRender());
        render.addKeyValueRender("template-mapping", new TemplatesMappingKeyValueRender());
        render.addKeyValueRender("freebase", new FreebaseKeyValueRender());

        // Within wikitext-json.structure element.
        render.addNodeRender("reference", new ReferenceNodeRender());
        render.addNodeRender("link"     , new LinkNodeRender());
        render.addNodeRender("list"     , new ListNodeRender());
        render.addNodeRender("section"  , new SectionRender());
        render.addNodeRender("template" , new TemplateNodeRender());
        render.addNodeRender("table"    , new TableNodeRender());
        render.addKeyValueRender("url"        , new URLKeyValueRender());
        render.addKeyValueRender("archiveurl" , new URLKeyValueRender());
        render.addKeyValueRender("title"      , new TitleKeyValueRender());
        render.addKeyValueRender("content"    , new ContentKeyValueRender());

        render.addPrimitiveRender( new BaseTextPrimitiveNodeRender() );
        return render;
    }

    @Override
    public DefaultHTMLRender createRender() {
        return createRender(true);
    }

    private DefaultHTMLRenderFactory() {}

}
