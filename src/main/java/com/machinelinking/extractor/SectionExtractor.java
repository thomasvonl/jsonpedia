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

import com.machinelinking.pagestruct.Ontology;
import com.machinelinking.serializer.Serializer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Specific {@link Extractor} for <i>Wikipedia section</i>s.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class SectionExtractor extends Extractor {

    private List<Section> sections;
    private Deque<Integer> stack;

    public SectionExtractor() {
        super(Ontology.SECTIONS_FIELD);
    }

    @Override
    public void section(String title, int level) {
        if(sections == null) sections = new ArrayList<>();
        if(stack == null) stack = new ArrayDeque<>();

        // use a stack to keep the parents
        // if we have the following structure
        // S1
        // -- S2
        // -- -- S3
        // S4
        // the stack will need to pop twice to jump from S3 to S4
        while(stack.size() > level){
            stack.pop();
        }

        sections.add(new Section(title, toIntArray(stack), level));
        stack.push(sections.size() -1); // for the next section, I'm the parent
    }

    @Override
    public void flushContent(Serializer serializer) {
        if(sections == null) {
            serializer.value(null);
            return;
        }
        serializer.openList();
        for(Section section : sections) {
            section.serialize(serializer);
        }
        serializer.closeList();
        sections.clear();
    }

    @Override
    public void reset() {
        if(sections != null) sections.clear();
        if(stack != null) stack.clear();
    }

    private int[] toIntArray(Deque<Integer> in) {
        int[] out = new int[in.size()];
        int index = 0;
        for(int i : in) {
            out[index++] = i;
        }
        Arrays.sort(out);
        return out;
    }

}
