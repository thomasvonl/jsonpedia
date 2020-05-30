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

import com.machinelinking.pagestruct.Ontology;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link NodeRender} imeplementation for <i>Wikitext table</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class TableNodeRender implements NodeRender {

    private static final Map<String,String> TABLE_ATTR = new HashMap<String,String>(){{
        put("class", "table");
    }};

    @Override
    public boolean acceptNode(JsonContext context, JsonNode node) {
        return true;
    }

    @Override
    public void render(JsonContext context, RootRender rootRender, JsonNode node, HTMLWriter writer)
    throws NodeRenderException {
        final JsonNode content = node.get(Ontology.CONTENT_FIELD);
        try {
            writer.openTable("Table", TABLE_ATTR);

            writer.openTableRow();
            JsonNode cell;
            for (int i = 0; i < content.size(); i++) {
                cell = content.get(i);
                if (isHeadCell(cell)) {
                    writer.openTableCol();
                    rootRender.render(context, rootRender, cell, writer);
                    writer.closeTableCol();
                }
            }
            writer.closeTableRow();

            for (int i = 0; i < content.size(); i++) {
                cell = content.get(i);
                if (isBodyCell(cell)) {
                    writer.openTableRow();
                    final JsonNode rowContent = cell.get(Ontology.CONTENT_FIELD);
                    for (int j = 0; j < rowContent.size(); j++) {
                        writer.openTableCol();
                        rootRender.render(context, rootRender, rowContent.get(j), writer);
                        writer.closeTableCol();
                    }
                    writer.closeTableRow();
                }
            }

            writer.closeTable();
        } catch (IOException ioe) {
            throw new NodeRenderException(ioe);
        }
    }

    private boolean isHeadCell(JsonNode node) {
        return checkCellType(node, "head_cell");
    }

    private boolean isBodyCell(JsonNode node) {
        return checkCellType(node, "body_cell");
    }

    private boolean checkCellType(JsonNode node, String t) {
        final JsonNode type = node.get(Ontology.TYPE_FIELD);
        return type != null && t.equals(type.asText());
    }

}
