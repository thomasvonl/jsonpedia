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

import com.machinelinking.parser.ParserLocation;
import com.machinelinking.serializer.Serializable;
import com.machinelinking.serializer.Serializer;

/**
 * Defines any issue raised while processing extraction.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class Issue implements Serializable {

    public enum Type {
        Error,
        Warning
    }

    private Type   type;
    private String description;
    private ParserLocation location;

    public Issue(Type type, String description, ParserLocation location) {
        this.type = type;
        this.description = description;
        this.location = location;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getRow() {
        return location.getRow();
    }

    public int getCol() {
        return location.getCol();
    }

    public void serialize(Serializer serializer) {
        serializer.openObject();
        serializer.fieldValue("type", type.toString());
        serializer.fieldValue("description", description);
        serializer.fieldValue("row", getRow());
        serializer.fieldValue("col", getCol());
        serializer.closeObject();
    }

}
