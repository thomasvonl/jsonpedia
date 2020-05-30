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

import com.machinelinking.filter.DefaultJSONFilterEngine;
import com.machinelinking.filter.JSONFilter;
import com.machinelinking.parser.DocumentSource;
import com.machinelinking.parser.WikiTextParserException;
import com.machinelinking.pipeline.FlagSet;
import com.machinelinking.pipeline.WikiPipeline;
import com.machinelinking.pipeline.WikiPipelineFactory;
import com.machinelinking.render.DefaultDocumentContext;
import com.machinelinking.render.DefaultHTMLRenderFactory;
import com.machinelinking.render.DocumentContext;
import com.machinelinking.render.NodeRenderException;
import com.machinelinking.serializer.JSONSerializer;
import com.machinelinking.template.RenderScope;
import com.machinelinking.util.JSONUtils;
import com.machinelinking.wikimedia.WikiAPIParserException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.util.TokenBuffer;
import org.xml.sax.SAXException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation for {@link AnnotationService}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
@Path("/annotate")
public class DefaultAnnotationService extends ServiceBase implements AnnotationService {

    public static final boolean FORMAT_JSON = false;

    public enum OutputFormat {
        json,
        html
    }

    @Path("/flags/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Override
    public FlagSet flags() {
        super.checkQuota();
        return FlagSetWrapper.getInstance();
    }

    @Path("/resource/{outFormat}/{resource}")
    @GET
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
            MediaType.TEXT_HTML + ";charset=UTF-8"
    })
    @Override
    public Response annotateResource(
            @PathParam("resource") String resource,
            @PathParam("outFormat")String outFormat,
            @QueryParam("procs")   String processors,
            @QueryParam("filter")  String filter
    ) {
        super.checkQuota();
        try {
            final DocumentSource documentSource = new DocumentSource(JSONUtils.toResourceURL(resource));
            return annotateDocumentSource(documentSource, processors, outFormat, filter);
        } catch (IllegalArgumentException iae) {
            throw new InvalidRequestException(iae);
        } catch (UnknownHostException uhe) {
            throw new UnreachableWikipediaServiceException(uhe);
        } catch (WikiAPIParserException wape) {
            throw new UnresolvableEntityException(wape);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalErrorException(e);
        }
    }

    @Path("/resource/{outFormat}/{resource}")
    @POST
    @Produces({
            MediaType.APPLICATION_JSON + ";charset=UTF-8",
            MediaType.TEXT_HTML + ";charset=UTF-8"
    })
    @Override
    public Response annotateResource(
            @PathParam("resource") String resource,
            @PathParam("outFormat")String outFormat,
            @FormParam("procs")    String processors,
            @FormParam("wikitext") String wikitext,
            @FormParam("filter")   String filter
    ) {
        super.checkQuota();
        try {
            final DocumentSource documentSource = new DocumentSource(JSONUtils.toResourceURL(resource), wikitext);
            return annotateDocumentSource(documentSource, processors, outFormat, filter);
        } catch (IllegalArgumentException iae) {
            throw new InvalidRequestException(iae);
        } catch (UnknownHostException uhe) {
            throw new UnreachableWikipediaServiceException(uhe);
        } catch (WikiAPIParserException wape) {
            throw new UnresolvableEntityException(wape);
        } catch (Exception e) {
            throw new InternalErrorException(e);
        }
    }

    private Response annotateDocumentSource(
            DocumentSource documentSource,
            String flags,
            String outFormat,
            String filterExp
    ) throws InterruptedException, SAXException, WikiTextParserException, ExecutionException, IOException, NodeRenderException {
        final OutputFormat format = checkOutFormat(outFormat);
        final WikiPipeline wikiEnricher = WikiPipelineFactory
                .getInstance()
                .createFullyConfiguredInstance(flags, WikiPipelineFactory.DEFAULT_FLAGS);
        final JSONSerializer jsonSerializer;
        final JSONFilter filter;
        try {
            filter = DefaultJSONFilterEngine.parseFilter(filterExp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while parsing filter.", e);
        }

        final TokenBuffer buffer = JSONUtils.createJSONBuffer();
        try {
            jsonSerializer = new JSONSerializer(buffer);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error while initializing serializer.", ioe);
        }

        wikiEnricher.enrichEntity(documentSource, jsonSerializer);
        return toOutputFormat(documentSource.getDocumentURL(), buffer, format, filter);
    }

    private OutputFormat checkOutFormat(String outFormat) {
        try {
            return OutputFormat.valueOf(outFormat);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unsupported output format: [%s]", outFormat));
        }
    }

    private Response toOutputFormat(URL documentURL, TokenBuffer buffer, OutputFormat format, JSONFilter filter)
            throws IOException, NodeRenderException {
        switch(format) {
            case json:
                return Response.ok(
                        JSONUtils.bufferToJSONString(
                                JSONUtils.createResultFilteredObject(buffer, filter), FORMAT_JSON
                        ),
                        MediaType.APPLICATION_JSON + ";charset=UTF-8"
                ).build();
            case html:
                final JsonNode rootNode = JSONUtils.bufferToJSONNode(buffer);
                final JsonNode target   =
                        filter.isEmpty() ? rootNode : JSONUtils.bufferToJSONNode(
                                JSONUtils.createResultFilteredObject(rootNode, filter)
                        );
                final DocumentContext context = new DefaultDocumentContext(
                        RenderScope.FULL_RENDERING,
                        documentURL
                );
                return Response.ok(
                        DefaultHTMLRenderFactory.getInstance().createRender().renderDocument(context, target),
                        MediaType.TEXT_HTML + ";charset=UTF-8"
                ).build();
            default:
                throw new IllegalArgumentException("Unsupported conversion to " + format);
        }
    }

}
