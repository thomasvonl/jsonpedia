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

package com.machinelinking.splitter;

import com.machinelinking.parser.DefaultWikiTextParserHandler;
import com.machinelinking.serializer.Serializable;
import com.machinelinking.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Splitter} allows to redirect specific {@link DefaultWikiTextParserHandler} events representing an object
 * toward different consumers. The redirection criteria is based on the visit depth.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class Splitter extends DefaultWikiTextParserHandler implements Serializable {

    private static int COUNTER = 0;

    private final String name;

    private final List<WikiTextParserHandlerEventBuffer> buffers = new ArrayList<>();

    private WikiTextParserHandlerSplitter handlerSplitter;

    protected Splitter(String name) {
        if(name == null) throw new IllegalArgumentException();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void split() {
        final WikiTextParserHandlerEventBuffer buffer = handlerSplitter.createRedirection(
                String.format("%s_%d", name, COUNTER++)
        );
        buffers.add(buffer);
    }

    public void initHandlerSplitter(WikiTextParserHandlerSplitter handlerSplitter) {
        if(this.handlerSplitter != null) throw new IllegalStateException();
        this.handlerSplitter = handlerSplitter;
    }

    public void reset() {
        handlerSplitter = null;
        buffers.clear();
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.field(name);
        if(buffers.isEmpty()) {
            serializer.value(null);
        } else {
            serializer.openList();
            for (WikiTextParserHandlerEventBuffer buffer : buffers) {
                buffer.flush(serializer);
            }
            serializer.closeList();
            buffers.clear();
        }
    }
}
