# JSONpedia README

# Quick Tutorial

The code snippet below shows how to retrieve the JSON structure of the WikiText DOM of the http://en.wikipedia.org/wiki/London page.
Then a filtering over sections and over references inside sections is performed, the fitered elements are rendered as HTML. 

```java
import com.machinelinking.main.JSONpedia;
import org.codehaus.jackson.JsonNode;

JSONpedia jsonpedia = JSONpedia.instance();
JsonNode root = jsonpedia.process("en:London").flags("Structure").json();

JsonNode[] sections = jsonpedia.applyFilter("@type:section", root);
String firstSectionHTML = jsonpedia.render("en:London", sections[0]);

JsonNode[] allReferencesInSections = jsonpedia.applyFilter("@type:section>@type:reference", root);
String allReferencesHTML = jsonpedia.render("en:London", allReferencesInSections);
```

## What is JSONpedia?
       
JSONpedia is a framework designed to simplify access at MediaWiki contents transforming everything into JSON. 
Such framework provides a library, a REST service and CLI tools to parse, convert, enrich and store WikiText documents. 
In order to facilitate the consumption of the huge availability of the MediaWiki semi-structured contents, 
the converted JSON documents are stored both into ElasticSearch (providing advanced faceting support) and 
MongoDB (allowing distributed map/reduce tasks). 
JSONpedia supplies capabilities for recursive template expansion and mapping to DBpedia. 
This framework has been initially designed to extract linguistic resources from the Wikipedia dumps and to enable massive data scraping, the present intent of the project is to implement a general purpose infrastructure enabling Wikipedia multi language data consumption both for researchers and industry.

The online demo version can be found at [jsonpedia.org](http://jsonpedia.org).

Please also see our Slideshare [presentation](http://www.slideshare.net/michele.mostarda/jsonpedia-intro).

## Main features
- Fast event based [WikiText](http://en.wikipedia.org/wiki/Wiki_markup) Parser
- Support for [template](http://en.wikipedia.org/wiki/Wikipedia:Templates) expansion
- Facilities and code samples (test cases) to build complex WikiText processing pipelines (filters, transformers, enrichers, validators and so on)
- [REST](http://en.wikipedia.org/wiki/Representational_state_transfer) service to perform live MediaWiki pages conversion and advanced queries over both [Elasticsearch](http://www.elasticsearch.org/) and [MongoDB](http://www.mongodb.org/)
- Web frontend to play with the main features
- CLI tools to process MediaWiki dumps and store them into Elasticsearch and MongoDB, as well performing data processing using UNIX command pipelines
- Multilingual.

## Full list of features
- WikiText event parser with mashed support for WikiMarkup and XML
- WikiMedia template processing support
- Configurable event processing pipeline
- DBpedia template mapping integration
- HTML rendering support
- Freebase enrichment
- Elasticsearch storage support
- MongoDB storage support
- RESTful access to backend (live processing pipeline, Elasticsearch and MongoDB query)
- Web frontend with Demo and documentation
- Sample code to implement filters, transformers, enrichers, validators
- UNIX CLI support
- Full multilingual support
## Documentation

Please see [documentation](/hardest/jsonpedia/src/HEAD/docs/documentation.md) for coding references
and [architecture](/hardest/jsonpedia/src/HEAD/docs/architecture.md) for architectural diagrams.

## Requirements
- Maven 3+
- Optionally ElasticSearch 1.1
- Optionally MongoDB 2.6

## Compile and package JSONpedia
JSONpedia can be compiled and packaged by issuing the following command:

```bash
$ mvn package  # this will run tests
```

Some of the tests might fail as they expect to find MongoDB and Elasticsearch installed and running.

To package JSONpedia without running tests execute:
```bash
mvn -DskipTests package
```

When the compilation finishes the resulting fully contained JAR can be found in ```target/```
with the name ```jsonpedia-{VERSION}.jar```.

## Run the web interface

```bash
$ echo "server.host = 127.0.0.1" > /tmp/conf.properties
$ echo "server.port = 8080" > /tmp/conf.properties
$ java -cp build/libs/jsonpedia-{VERSION}.jar com.machinelinking.cli.server -c /tmp/conf.properties
```

You can now connect to ```http://127.0.0.1:8080/frontend/index.html``` in your browser and use the web interface.

## Run the Storage Loader

The _loader.py_ tool helps to process the latest online Wikipedia dumps and index them into the configured storages.
Details about the usage of this script can be found in its header.

To perform massing loading of first 10 Wikipedia dumps using the default configuration run: 

```bash
$ bin/loader.py conf/default.properties 10
```
You will see an output log similar to the following one:

```bash
Retrieved latest articles links: [ ... 'enwiki-latest-pages-articles27.xml-p029625017p043536594.bz2']
Processing article 0 - link: enwiki-latest-pages-articles1.xml-p000000010p000010000.bz2 file: work/enwiki-latest-pages-articles1.xml-p000000010p000010000.bz2
Start download ...
Download complete in dd sec.
Start ingestion ...
Ingestion completed in dd sec.
[...]
```

To process a single dump manually simply run:

```bash
$ java -cp build/libs/jsonpedia-{VERSION}.jar \
  com.machinelinking.cli.loader conf/default.properties src/test/resources/enwiki-latest-pages-articles-p3.xml.gz
```

```bash
Processing with 2 threads
Closing process...
Done.
processor: Processed pages: 25, elapsed time: 0 (ms), exceptions: []
templates: 1117, properties 4907, max properties/template: 52, avg properties/template: 4,393017
```

## Run the Storage Facet Loader

The _facet_loader.py_ tool helps to produce a facet index based on an existing index in Elasticsearch.
Details about the usage of this script can be found in its header.

Example: process all documents in db 'jsonpedia_test_load' collection 'en' following configuration specified in 
'faceting.properties' and store the generated documents in db 'jsonpedia_test_facet' collection en.

```bash
$ bin/facet_loader.py -s localhost:9300:jsonpedia_test_load:en -d localhost:9300:jsonpedia_test_facet:en \
        -l 100 -c conf/faceting.properties
```

```bash
...
Executing command:
MAVEN_OPTS='-Xms8g -Xmx8g -Dlog4j.configuration=file:conf/log4j.properties'
mvn exec:java -Dexec.mainClass=com.machinelinking.cli.facetloader
-Dexec.args='-s localhost:9300:jsonpedia_test_load:en -d localhost:9300:jsonpedia_test_facet:en -l 100 -c conf/faceting.properties'
...
Facet Loading Report:
Processed docs: 58, Generated facet docs: 1051
```

## Run the CSV Exporter

The CSV Exporter CLI tool allows to convert Wikipedia dumps to tabular data generated from page parsing.

To convert the gzipped dump in test resources using the page prefix of en Wikipedia (with a single thread processor) run:

```bash
$ java -cp build/libs/jsonpedia-{VERSION}.jar com.machinelinking.cli.exporter \
    --prefix http://en.wikipedia.org \
    --in src/test/resources/dumps/enwiki-latest-pages-articles-p1.xml.gz \
    --out out.csv --threads 1
```

# License

This software is copyright of Michele Mostarda (me@michelemostarda.it) and released under [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).

All the graphic material (diagrams, pictures and slides) is released under Creative Commons Attribution
![Creative Commons Attribution 4.0 International License Logo](https://i.creativecommons.org/l/by/4.0/88x31.png "Creative Commons Attribution 4.0 International License")