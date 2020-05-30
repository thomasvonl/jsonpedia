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
 * Defines the factory for a {@link com.machinelinking.storage.JSONStorageConfiguration}
 * and a {@link com.machinelinking.storage.JSONStorage}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface JSONStorageFactory<C extends JSONStorageConfiguration, S extends JSONStorage, D extends Document> {

    C createConfiguration(String configURI);

    S createStorage(C config, DocumentConverter<D> converter);

    S createStorage(C config);

}
