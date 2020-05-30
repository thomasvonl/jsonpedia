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

package com.machinelinking.storage;

/**
 * Defines the resultset of a {@link com.machinelinking.storage.Selector}.
 *
 * @see com.machinelinking.storage.Selector
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface ResultSet<D extends Document> {

    /**
     * @return number of total results.
     */
    long getCount();

    /**
     * @return next document or <code>null</code>.
     */
    D next();

    /**
     * Closes the resultset cursor.
     */
    void close();

}
