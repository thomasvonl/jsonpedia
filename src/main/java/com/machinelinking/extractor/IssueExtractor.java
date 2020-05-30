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
import com.machinelinking.parser.ParserLocation;
import com.machinelinking.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link Extractor} collects issues during extraction.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class IssueExtractor extends Extractor {

    private List<Issue> issues = null;

    public IssueExtractor() {
        super(Ontology.ISSUES_FIELD);
    }

    @Override
    public void parseWarning(String msg, ParserLocation location) {
        if(issues == null) issues = new ArrayList<Issue>();
        issues.add( new Issue(Issue.Type.Warning, msg, location) );
    }

    @Override
    public void parseError(Exception e, ParserLocation location) {
        if(issues == null) issues = new ArrayList<Issue>();
        issues.add( new Issue(Issue.Type.Warning, e.getMessage(), location) );
    }

    @Override
    public void flushContent(Serializer serializer) {
        if(issues == null) {
            serializer.value(null);
            return;
        }
        serializer.openList();
        for(Issue issue : issues) {
            issue.serialize(serializer);
        }
        serializer.closeList();
        issues.clear();
    }

    @Override
    public void reset() {
        if(issues != null) issues.clear();
    }
}
