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

package com.machinelinking.cli;

import com.machinelinking.pipeline.Flag;
import com.machinelinking.pipeline.WikiPipelineFactory;
import com.machinelinking.storage.DefaultJSONStorageLoader;
import com.machinelinking.storage.JSONStorage;
import com.machinelinking.storage.JSONStorageConfiguration;
import com.machinelinking.storage.JSONStorageFactory;
import com.machinelinking.storage.MultiJSONStorageFactory;
import com.machinelinking.storage.StorageLoaderReport;
import com.machinelinking.util.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * CLI interface to run the {@link com.machinelinking.storage.DefaultJSONStorageLoader}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class loader {

    public static final String LOADER_STORAGE_FACTORY_PROP = "loader.storage.factory";
    public static final String LOADER_STORAGE_CONFIG_PROP  = "loader.storage.config";
    public static final String LOADER_PREFIX_URL_PROP      = "loader.prefix.url";
    public static final String LOADER_FLAGS_PROP           = "loader.flags";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: $0 <config-file> <dump>");
            System.exit(1);
        }

        try {
            final File configFile = check(args[0]);
            final File dumpFile = check(args[1]);
            final Properties properties = new Properties();
            properties.load(FileUtils.openInputStream(configFile));

            final Flag[] flags = WikiPipelineFactory.getInstance().toFlags(
                    getPropertyOrFail(
                            properties,
                            LOADER_FLAGS_PROP,
                            "valid flags: " + Arrays.toString(WikiPipelineFactory.getInstance().getDefinedFlags())
                    )
            );
            final JSONStorageFactory jsonStorageFactory = MultiJSONStorageFactory.loadJSONStorageFactory(
                    getPropertyOrFail(
                            properties,
                            LOADER_STORAGE_FACTORY_PROP,
                            null
                    )
            );
            final String jsonStorageConfig = getPropertyOrFail(
                    properties,
                    LOADER_STORAGE_CONFIG_PROP,
                    null
            );
            final URL prefixURL = readURL(
                    getPropertyOrFail(
                            properties,
                            LOADER_PREFIX_URL_PROP,
                            "expected a valid URL prefix like: http://en.wikipedia.org/"
                    ),
                    LOADER_PREFIX_URL_PROP
            );

            final DefaultJSONStorageLoader[] loader = new DefaultJSONStorageLoader[1];
            final boolean[] finalReportProduced = new boolean[]{false};
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!finalReportProduced[0] && loader[0] != null) {
                        System.err.println("Process interrupted. Partial loading report: " + loader[0].createReport());
                    }
                    System.err.println("Shutting down.");
                }
            }));

            final JSONStorageConfiguration storageConfig = jsonStorageFactory.createConfiguration(jsonStorageConfig);
            try (final JSONStorage storage = jsonStorageFactory.createStorage(storageConfig)) {
                loader[0] = new DefaultJSONStorageLoader(
                        WikiPipelineFactory.getInstance(), flags, storage
                );

                final StorageLoaderReport report = loader[0].load(
                        prefixURL,
                        FileUtil.openDecompressedInputStream(dumpFile)
                );
                System.err.println("Loading report: " + report);
                finalReportProduced[0] = true;
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static File check(String file) {
        final File f = new File(file);
        if (!f.exists()) throw new IllegalArgumentException(String.format("Invalid file: %s", f.getAbsolutePath()));
        return f;
    }

    private static URL readURL(String url, String desc) {
        try {
            return new URL(url);
        } catch (MalformedURLException murle) {
            throw new IllegalStateException(String.format("Invalid URL specified for [%s]", desc), murle);
        }
    }

    private static String getPropertyOrFail(Properties properties, String property, String errMsg) {
        final String value = properties.getProperty(property);
        if (value == null) throw new IllegalArgumentException(
                String.format("Invalid properties file: must define property [%s] - %s.", property, errMsg)
        );
        return value;
    }

}
