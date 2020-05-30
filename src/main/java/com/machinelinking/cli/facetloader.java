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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorage;
import com.machinelinking.storage.elasticsearch.ElasticJSONStorageFactory;
import com.machinelinking.storage.elasticsearch.ElasticSelector;
import com.machinelinking.storage.elasticsearch.faceting.DefaultElasticFacetConfiguration;
import com.machinelinking.storage.elasticsearch.faceting.DefaultElasticFacetManager;
import com.machinelinking.storage.elasticsearch.faceting.ElasticFacetManager;
import com.machinelinking.storage.elasticsearch.faceting.EnrichedEntityFacetConverter;
import com.machinelinking.storage.elasticsearch.faceting.FacetLoadingReport;

import java.io.File;

/**
 * CLI interface to run the {@link com.machinelinking.storage.elasticsearch.faceting.DefaultElasticFacetManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class facetloader {

    @Parameter(
            names = {"--source", "-s"},
            description = "Source storage URI",
            required = true
    )
    private String source;

    @Parameter(
            names = {"--destination", "-d"},
            description = "Destination storage URI",
            required = true
    )
    private String destination;

    @Parameter(
            names = {"--limit", "-l"},
            description = "Document limit",
            required = true
    )
    private int limit;

    @Parameter(
            names = {"--conf", "-c"},
            description = "Faceting config",
            validateValueWith = CLIUtils.ExistingFile.class,
            required = true
    )
    private File conf;

    public static void main(String[] args) {
        facetloader cli = new facetloader();
        System.exit( cli.run(args) );
    }

    public int run(String[] args) {
          final JCommander commander = new JCommander(this);
          int exitCode;
          try {
              commander.parse(args);
              final ElasticJSONStorageFactory factory = new ElasticJSONStorageFactory();
              final ElasticJSONStorage fromStorage = factory.createStorage(factory.createConfiguration(source));
              final ElasticJSONStorage facetStorage = factory.createStorage(factory.createConfiguration(destination));
              facetStorage.deleteCollection();
              final DefaultElasticFacetConfiguration configuration = new DefaultElasticFacetConfiguration(
                      conf,
                      limit,
                      fromStorage,
                      facetStorage
              );
              final ElasticFacetManager manager = new DefaultElasticFacetManager(configuration);
              final ElasticSelector selector = new ElasticSelector();
              final FacetLoadingReport report = manager.loadFacets(selector, new EnrichedEntityFacetConverter());
              System.out.println(report);
              exitCode = 0;
          } catch (ParameterException pe) {
              System.err.println(pe.getMessage());
              commander.usage();
              exitCode = 1;
          } catch (Exception e) {
              System.err.println("Error while loading faceting index.");
              e.printStackTrace();
              exitCode = 3;
          }
          return exitCode;
      }


}
