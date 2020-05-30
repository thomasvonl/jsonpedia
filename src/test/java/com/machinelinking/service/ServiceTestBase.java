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

import com.machinelinking.util.JSONUtils;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ServiceTestBase {

    private final BasicServer server = new BasicServer();

    @BeforeMethod
    public void setUp() throws IOException {
        server.setUp();
    }

    @AfterMethod
    public void tearDown() {
        server.tearDown();
    }

    protected UriBuilder buildPath(Class<? extends Service> clazz, String path) throws URISyntaxException {
        return UriBuilder.fromResource(clazz)
            .uri(new URI(String.format("http://%s", BasicServer.DEFAULT_HOST))).port(BasicServer.DEFAULT_PORT)
            .path(path);
    }

    protected JsonNode performQuery(URI uri) throws IOException, ConnectionException {
        final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        try (final InputStream is = connection.getInputStream()) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(is));
            final StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
            return JSONUtils.parseJSON(content.toString());
        } catch (IOException ioe) {
            throw new ConnectionException(connection.getResponseCode());
        }
    }

    protected void performQueryAndCheckError(int responseCode, URI uri) throws IOException {
        try {
            performQuery(uri);
            Assert.fail("This test is expected to fail.");
        } catch (ConnectionException ce) {
            Assert.assertEquals(ce.errorCode, responseCode, "Invalid connection exception.");
        }
    }

    class ConnectionException extends Exception {
        final int errorCode;
        ConnectionException(int errorCode) {
            this.errorCode = errorCode;
        }
    }

}
