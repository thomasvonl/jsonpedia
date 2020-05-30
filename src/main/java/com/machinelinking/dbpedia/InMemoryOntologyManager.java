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

import com.machinelinking.util.JSONUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * In memory hashmap implementation of {@link OntologyManager}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class InMemoryOntologyManager implements OntologyManager {

    public static final String DBPEDIA_ONTOLOGY_PREFIX = "http://dbpedia.org/ontology/";

    public static final String XSD_PREFIX = "http://www.w3.org/2001/XMLSchema#";

    private static final String SERVICE = "http://dbpedia.org/sparql?query=%s&format=json";

    private static final String QUERY =
            "SELECT DISTINCT * WHERE {\n" +
            "{?p a <http://www.w3.org/2002/07/owl#ObjectProperty>     . " +
                    "OPTIONAL {?p rdfs:domain ?domain; rdfs:range ?range} . ?p rdfs:label ?label}\n" +
            "UNION\n" +
            "{?p a <http://www.w3.org/2002/07/owl#DatatypeProperty> . " +
                    "OPTIONAL {?p rdfs:domain ?domain; rdfs:range ?range} . ?p rdfs:label ?label}\n" +
            "FILTER langMatches( lang(?label), \"EN\" )\n" +
            "}";

    private static final Logger logger = Logger.getLogger(InMemoryOntologyManager.class);

    private final Map<String, Property> mapping;

    public static Map<String, Property> initOntologyIndex(boolean force) throws OntologyManagerException {
        final File serializationFile = getSerializationFile();
        if(!force && serializationFile.exists()) return loadOntologyIndex(serializationFile);


        final JsonNode result;
        try {
            final URL url = new URL(String.format(SERVICE, URLEncoder.encode(QUERY, "UTF-8")));
            final InputStream is = new BufferedInputStream( url.openStream() );
            result = JSONUtils.parseJSON(is);
        } catch (IOException ioe) {
            throw new OntologyManagerException(ioe);
        }
        Iterator<JsonNode> bindings = result.get("results").get("bindings").getElements();
        JsonNode current;
        String property;
        String enLabel;
        String domain;
        String range;
        Map<String, Property> ontology = new HashMap<>();
        int total = 0;
        int unique = 0;
        while(bindings.hasNext()) {
            current = bindings.next();
            property = normalizePrefix(current.get("p").get("value").asText());
            enLabel  = normalizePrefix(current.get("label").get("value").asText());
            domain   = normalizePrefix(JSONUtils.asPrimitiveString(optionallyGetFieldValue(current, "domain")));
            range    = normalizePrefix(JSONUtils.asPrimitiveString(optionallyGetFieldValue(current, "range")));
            Property found = ontology.put(property, new DefaultProperty(property, enLabel, domain, range));
            if(found == null) unique++;
            total++;
        }
        logger.info(String.format("Total properties: %d, unique values: %d", total, unique));
        saveOntologyIndex(ontology, serializationFile);
        return ontology;
    }

    public static Map<String, Property> initOntologyIndex() throws OntologyManagerException {
        return initOntologyIndex(false);
    }

    private static JsonNode optionallyGetFieldValue(JsonNode n, String field) {
        JsonNode f = n.get(field);
        return f == null ? null : f.get("value");
    }

    private static String normalizePrefix(String url) {
        if(url == null) return null;
        if(url.startsWith(DBPEDIA_ONTOLOGY_PREFIX)) {
            return url.substring(DBPEDIA_ONTOLOGY_PREFIX.length());
        } else if(url.startsWith(XSD_PREFIX)) {
            return String.format("xsd:%s", url.substring(XSD_PREFIX.length()));
        }
        return url;
    }

    private static void saveOntologyIndex(Map<String, ? extends Property> ontology, File serializationFile)
    throws OntologyManagerException {
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream(serializationFile)));
            oos.writeObject(ontology);
            oos.close();
        } catch (IOException ioe) {
            throw new OntologyManagerException(ioe);
        }
    }

    private static Map<String, Property> loadOntologyIndex(File serializationFile) throws OntologyManagerException {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(serializationFile)));
            final Map<String, Property> mapping =
                    (Map<String, Property>) ois.readObject();
            ois.close();
            return mapping;
        } catch (IOException | ClassNotFoundException ioe) {
            throw new OntologyManagerException(ioe);
        }
    }

    private static File getSerializationFile() {
        return new File("work/ontology.ser");
    }

    public InMemoryOntologyManager(Map<String, Property> mapping) throws OntologyManagerException {
        this.mapping = mapping;
    }

    public InMemoryOntologyManager() throws OntologyManagerException {
        this( initOntologyIndex() );
    }

    @Override
    public int getPropertiesCount() {
        return mapping.size();
    }

    @Override
    public Set<String> getPropertyNames() {
        return new HashSet<>( mapping.keySet() );
    }

    @Override
    public Property getProperty(String property) {
        return mapping.get(property);
    }

}
