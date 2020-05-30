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

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Basic Service implementation based on <i>Grizzly</i>.
 *
 * See: http://mytecc.wordpress.com/2013/06/06/grizzly-2-3-3-serving-static-http-resources-from-jar-files/
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class BasicServer {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final Integer DEFAULT_PORT  = 9998;

    public static final String RESOURCES_ROOT = "./static_resources";

    private static final Logger logger = Logger.getLogger(BasicServer.class);

    private static final String PACKAGE_ROOT = BasicServer.class.getPackage().getName().replace(".", "/");

    private static final String SRC_RESOURCE_ROOT = String.format("src/main/resources/%s/frontend", PACKAGE_ROOT);
    private static final String JAR_RESOURCE_ROOT = String.format("%s/%s/frontend", RESOURCES_ROOT, PACKAGE_ROOT);

    private static final String CONTENT_TYPE = "Content-Type";

    public final URI baseURI;

    private HttpServer httpServer;

    public BasicServer(String host, int port) {
        this.baseURI = UriBuilder.fromUri( String.format("http://%s/", host)).port(port).build();
    }

    public BasicServer() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setUp() throws IOException {
        ResourceConfig rc = new PackagesResourceConfig(BasicServer.class.getPackage().getName());
        httpServer = GrizzlyServerFactory.createHttpServer(getBaseURI(), rc);
        final StaticHttpHandler handler =
            // Provided by Grizzly 2.3.3 that unfortunately doesn't manage correctly encoded URLs in GET requests.
            // new CLStaticHttpHandler( this.getClass().getClassLoader() ){
            new StaticHttpHandler( initFrontendResources() ) {
                @Override
                protected boolean handle(String uri, Request req, Response res) throws Exception {
                    if(uri.endsWith(".html")) {
                        res.setHeader(CONTENT_TYPE, "text/html; charset=utf-8");
                        res.setHeader("Access-Control-Allow-Origin", "*");
                    } else if(uri.endsWith(".css")) {
                        res.setHeader(CONTENT_TYPE, "text/css");
                    } else if(uri.endsWith(".js")) {
                        res.setHeader(CONTENT_TYPE, "text/javascript");
                        res.setHeader("Access-Control-Allow-Origin", "*");
                    } else if(uri.endsWith(".png")) {
                        res.setHeader(CONTENT_TYPE, "image/png");
                    } else if(uri.endsWith(".gif")) {
                        res.setHeader(CONTENT_TYPE, "image/gif");
                    }
                    return super.handle(uri, req, res);
                }
            };
        // Disabled file cache to ensure setup of Access-Control-Allow-Origin in HTTP header.
        handler.setFileCacheEnabled(false);
        httpServer.getServerConfiguration().addHttpHandler(
                handler,
                "/frontend/"
        );
        httpServer.start();
        logger.info("Started Grizzly server: " + getBaseURI());
    }

    public void tearDown() {
        httpServer.stop();
    }

    private String initFrontendResources() throws IOException {
        final URL container = this.getClass().getClassLoader().getResource(PACKAGE_ROOT);
        final String containerProtocol = container.getProtocol();
        switch (containerProtocol) {
            case "file":
                return SRC_RESOURCE_ROOT;
            case "jar":
                decompress(container.getFile().substring("file:".length()).split("!")[0], PACKAGE_ROOT, RESOURCES_ROOT);
                return JAR_RESOURCE_ROOT;
            default:
                throw new IllegalStateException("Invalid protocol for container: " + containerProtocol);
        }
    }

    private void decompress(String jarFile, String filter, String destination) throws IOException {
        new File(destination).delete();
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();
        String entryName;
        while (entries.hasMoreElements()){
            JarEntry entry = entries.nextElement();
            entryName = entry.getName();
            if(! entryName.startsWith(filter)) continue;
            if(entryName.endsWith(".class")) continue;
            File file = new java.io.File(destination, entryName);
            if (entry.isDirectory()) {
                file.mkdirs();
                continue;
            }
            byte[] buffer = new byte[1024 * 4];
            int readBytes;
            try (
                    final InputStream is = new BufferedInputStream(jar.getInputStream(entry));
                    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file))
            ) {
                while ((readBytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                }
            }
        }
    }

}
