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

package com.machinelinking.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Manages the service configuration.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ConfigurationManager {

    private static ConfigurationManager instance;

    private Properties properties;

    public static ConfigurationManager getInstance() {
        if(instance == null) instance = new ConfigurationManager();
        return instance;
    }

    private ConfigurationManager() {}

    public void initProperties(Properties properties) {
        if(this.properties != null) throw new IllegalStateException("Properties already initialized.");
        this.properties = properties;
    }

    public void initProperties(File file) {
        final Properties p = new Properties();
        try(FileInputStream fis = new FileInputStream(file)) {
            p.load(fis);
        } catch(Exception e) {
            throw new IllegalStateException("Error while reading configuration file.", e);
        }
        initProperties(p);
    }

    public String getProperty(String name, String defaultValue) {
        checkInitProperties();
        final String value = this.properties.getProperty(name, defaultValue);
        if(value == null)
            throw new IllegalArgumentException(String.format("Cannot find value for property '%s'", name));
        return value;
    }

    public String getProperty(String name) {
        return getProperty(name, null);
    }

    public boolean isInitialized() {
        return this.properties != null;
    }

    private void checkInitProperties() {
        if(this.properties == null) throw new IllegalStateException("Properties have not yet initialized.");
    }

}
