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

package com.machinelinking.freebase;

import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Provides enrichment on the <i>Google Freebase</i> service.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class FreebaseService {

    private static final String API_SERVICE = "https://www.googleapis.com/freebase/v1/";

    private static final String SEARCH_SERVICE = API_SERVICE + "search?query=%s";

    private static final String RESPONSE_OK  = "200 OK";
    private static final String RESULT_FIELD = "result";

    private static FreebaseService instance;

    public static FreebaseService getInstance() {
        if(instance == null) instance = new FreebaseService();
        return instance;
    }

    private FreebaseService() {}

    /**
     * Returns data for a given entity name.
     *
     * @param entityName
     * @return the node representing the fetched entity.
     * @throws IOException
     */
    public JsonNode getEntityData(String entityName) throws IOException {
        final URL query;
        try {
            query = new URL(
                    String.format(SEARCH_SERVICE, URLEncoder.encode(entityName, "UTF-8"))
            );
        } catch (Exception e) {
            throw new RuntimeException("Error while preparing query.");
        }
        JsonNode response = JSONUtils.parseJSON(query.openStream());
        final String status = response.get("status").asText();
        if( ! RESPONSE_OK.equals(status) ) throw new RuntimeException("Invalid response status: " + status);
        return response.get(RESULT_FIELD).get(0);
    }

}
