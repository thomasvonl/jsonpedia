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

package com.machinelinking.dbpedia;

import java.util.Set;

/**
 * Defines a manager for ontologies.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface OntologyManager {

    /**
     * Returns the number of stored properties.
     *
     * @return number of properties.
     */
    int getPropertiesCount();

    /**
     * Returns the set of property names.
     *
     * @return not <code>null</code> set of names.
     */
    Set<String> getPropertyNames();

    /**
     * Returns a {@link Property} for a given <i>property</i> name.
     *
     * @param property
     * @return a property mapping or <code>null</code> if not found.
     */
    Property getProperty(String property);

}
