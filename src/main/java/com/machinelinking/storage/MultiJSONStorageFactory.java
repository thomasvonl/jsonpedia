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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class MultiJSONStorageFactory
        extends AbstractJSONStorageFactory<MultiJSONStorageConfiguration, MultiJSONStorage, MultiDocument> {

    private final Map<JSONStorageConfiguration, JSONStorageFactory> configurationToFactory = new HashMap<>();

    public static JSONStorageFactory loadJSONStorageFactory(String className) {
        try {
            return (JSONStorageFactory) DefaultJSONStorageLoader.class.getClassLoader()
                    .loadClass(className).newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException( String.format("Invalid class name: %s .", className) );
        } catch (Exception e) {
            throw new IllegalArgumentException( String.format("Error while loading class: %s .", className), e);
        }
    }

    public JSONStorageConfiguration createSingleConfiguration(String configURI) {
        String[] configParts = configURI.split("\\|");
        if(configParts.length != 2)
            throw new IllegalArgumentException("Expected <JSONStorageFactory.path>|<paramsURI>");
        return instantiateConfiguration(configParts[0], configParts[1]);
    }

    public JSONStorage createSingleStorage(JSONStorageConfiguration configuration) {
        final JSONStorageFactory factory = configurationToFactory.get(configuration);
        return factory.createStorage(configuration);
    }

    @Override
    public MultiJSONStorageConfiguration createConfiguration(String configURI) {
        final String[] storageConfigs = configURI.split(";");
        final List<JSONStorageConfiguration> configurations = new ArrayList<>();
        for(String storageConfig : storageConfigs) {
            configurations.add(createSingleConfiguration(storageConfig));
        }
        return new MultiJSONStorageConfiguration(
                configurations.toArray(new JSONStorageConfiguration[configurations.size()])
        );
    }

    @Override
    public MultiJSONStorage createStorage(
            MultiJSONStorageConfiguration multiConfig, DocumentConverter<MultiDocument> converter
    ) {
        final List<JSONStorage> storages = new ArrayList<>();
        for(JSONStorageConfiguration config : multiConfig) {
            storages.add( createSingleStorage(config) );
        }
        return new MultiJSONStorage(multiConfig, converter, storages.toArray(new JSONStorage[storages.size()]));
    }

    @Override
    public MultiJSONStorage createStorage(MultiJSONStorageConfiguration config) {
        return createStorage(config, null);
    }

    private JSONStorageConfiguration instantiateConfiguration(String factoryClass, String configURI) {
        final JSONStorageFactory factory = loadJSONStorageFactory(factoryClass);
        final JSONStorageConfiguration configuration =  factory.createConfiguration(configURI);
        configurationToFactory.put(configuration, factory);
        return configuration;
    }

}
