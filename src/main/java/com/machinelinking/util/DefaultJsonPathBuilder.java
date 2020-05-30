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

package com.machinelinking.util;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Default implementation of {@link JsonPathBuilder}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultJsonPathBuilder implements JsonPathBuilder {

    private Stack<Element> stack = new Stack<>();

    @Override
    public void startPath() {
        stack.clear();
    }

    @Override
    public void enterArray() {
        stack.push( new ArrayElement() );
    }

    @Override
    public void arrayElem() {
        try {
            final ArrayElement arrayElement = (ArrayElement) stack.peek();
            arrayElement.incIndex();
        } catch(ClassCastException cce) {
            throw new IllegalStateException("Can be invoked within an array");
        }
    }

    @Override
    public void exitArray() {
        try {
            if (!(stack.pop() instanceof ArrayElement)) {
                throw new IllegalStateException("An array must be open first.");
            }
        } catch (EmptyStackException ese) {
            throw new IllegalStateException("Not open array.");
        }
    }

    @Override
    public void enterObject() {
        stack.push( new ObjectElement() );
    }

    @Override
    public void field(String fieldName) {
        try {
            final ObjectElement objectElement = (ObjectElement) stack.peek();
            objectElement.setField(fieldName);
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Can be invoked within an object");
        }
    }

    @Override
    public void exitObject() {
        try {
            if (!(stack.pop() instanceof ObjectElement)) {
                throw new IllegalStateException("An object must be open first.");
            }
        } catch (EmptyStackException ese) {
            throw new IllegalStateException("Not open object.");
        }

    }

    @Override
    public String getJsonPath() {
        final StringBuilder sb = new StringBuilder();
        sb.append('$');
        for(Element element : stack) {
            sb.append(element.getPathSeparator());
            sb.append(element.toJsonSection()   );
        }
        return sb.toString();
    }

    @Override
    public boolean subPathOf(JsonPathBuilder other, boolean strict) {
        final DefaultJsonPathBuilder otherBuilder = (DefaultJsonPathBuilder) other;
        final int stackSize      = stack.size();
        final int otherStackSize = otherBuilder.stack.size();
        if(strict) {
            if(stackSize != otherStackSize) return false;
        } else {
            if(otherStackSize > stackSize)  return false;
        }
        for(int i = 0; i < otherStackSize; i++) {
            if(! stack.get(i).equals(otherBuilder.stack.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getJsonPath();
    }

    private interface Element {
        String toJsonSection();
        String getPathSeparator();
    }

    private class ArrayElement implements Element {
        private int index = -1;
        void incIndex() {
            index++;
        }

        @Override
        public String toJsonSection() {
            return String.format("[%s]", index == -1 ? "*" : index);
        }

        @Override
        public String getPathSeparator() {
            return "";
        }

        @Override
        public String toString() {
            return toJsonSection();
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(obj == this) return true;
            if(! (obj instanceof ArrayElement)) return false;
            final ArrayElement other = (ArrayElement) obj;
            return index == -1 || other.index == -1 || index == other.index;
        }
    }

    private class ObjectElement implements Element {

        private String field = null;

        void setField(String fieldName) {
            if(fieldName == null || fieldName.trim().length() == 0)
                throw new IllegalArgumentException("Invalid field: " + fieldName);
            field = fieldName;
        }

        @Override
        public String toJsonSection() {
            return field == null ? "" : field;
        }

        @Override
        public String getPathSeparator() {
            return field == null ? "" : ".";
        }

        @Override
        public int hashCode() {
            return field == null ? 0 : field.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (!(obj instanceof ObjectElement)) return false;
            final ObjectElement other = (ObjectElement) obj;
            return field == null ? other.field == null : field.equals(other.field);
        }

        @Override
        public String toString() {
            return toJsonSection();
        }
    }

}