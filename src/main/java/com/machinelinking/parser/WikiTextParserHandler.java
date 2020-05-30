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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;

/**
 * Interface of events produced by {@link WikiTextParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface WikiTextParserHandler extends TagHandler {

    enum ListType {
        Unordered,
        Numbered
    }

    void beginDocument(URL document);

    void paragraph();

    void section(String title, int level);

    void parseWarning(String msg, ParserLocation location);

    void parseError(Exception e, ParserLocation location);

    @Push(node="reference", id=0)
    void beginReference(String label);

    @Pop(node="reference", id=0)
    void endReference(String label);

    @Push(node="link", id=0)
    void beginLink(URL url);

    @Pop(node="link", id=0)
    void endLink(URL url);

    @Push(node="list")
    void beginList();

    void listItem(ListType t, int level);

    @Pop(node="list")
    void endList();

    @Push(node="template", id=0)
    void beginTemplate(TemplateName name);

    @Pop(node="template", id=0)
    void endTemplate(TemplateName name);

    @Push(node="table")
    void beginTable();

    void headCell(int row, int col);

    void bodyCell(int row, int col);

    @Pop(node="table")
    void endTable();

    void parameter(String param);

    void entity(String form, char value);

    void var(Var var);

    void text(String content);

    void italicBold(int level);

    @ValidateStack
    void endDocument();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Push {
        String node();
        int id() default -1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Pop  {
        String node();
        int id() default -1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ValidateStack {}

    interface Value {
        String serialize();
    }

    class Var implements Value {
        public final String name;
        public final Value defaultValue;
        Var(String name, Value defaultValue) {
            if(name == null) throw new IllegalArgumentException();
            this.name = name;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return String.format("var: %s [%s]", name, defaultValue);
        }

        @Override
        public String serialize() {
            return String.format("<%s:%s>", name, defaultValue == null ? "" : defaultValue.serialize());
        }
    }

    class Const implements Value {
        public final String constValue;
        public Const(String constValue) {
            if(constValue == null) throw new IllegalArgumentException();
            this.constValue = constValue;
        }

        @Override
        public String toString() {
            return String.format("const: [%s]", constValue);
        }

        @Override
        public String serialize() {
            return constValue;
        }
    }

    class TemplateName {
        public final Value[] fragments;
        public final String plain;

        public TemplateName(Value[] fragments) {
            this.fragments = fragments;
            final StringBuilder sb = new StringBuilder();
            for(Value fragment : fragments) {
                sb.append(fragment.serialize());
            }
            plain = sb.toString();
        }

        public TemplateName(String singleFragment) {
            this(new Value[]{new Const(singleFragment)});
        }

        public boolean containsVar() {
            for(Value v : fragments) {
                if(v instanceof Var) return true;
            }
            return false;
        }

    }

}


