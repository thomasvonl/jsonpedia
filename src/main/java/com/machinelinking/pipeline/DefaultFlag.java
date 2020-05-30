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

package com.machinelinking.pipeline;

/**
 * Default {@link Flag} implementation.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class DefaultFlag implements Flag, Comparable {

    private final String id;
    private final String description;

    public DefaultFlag(String id, String description) {
        this.id          = id;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof Flag) {
            final Flag other = (Flag) obj;
            return id.equals(other.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Object o) {
        final Flag other = (Flag) o;
        return id.compareTo(other.getId());
    }
}
