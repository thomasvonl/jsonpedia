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

package com.machinelinking.template;

import com.machinelinking.render.HTMLWriter;
import com.machinelinking.render.NodeRenderException;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Default implementation of {@link TemplateProcessor}.
 * Wikimedia template processor based on documentation available at
 * <a href="http://en.wikipedia.org/wiki/Help:Template">Help:Template</a>
 * <a href="http://en.wikipedia.org/wiki/Help:Magic_words">Help:Magic_words</a>
 * Test with <a href="http://en.wikipedia.org/w/index.php?title=Wikipedia:Sandbox&action=submit">Wikipedia:Sandbox</a>
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
class DefaultTemplateProcessor implements TemplateProcessor {

    /**
     * From http://en.wikipedia.org/wiki/Help:Magic_words
     */
    public static  final Set<String> VARIABLES = Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(
            "ARTICLEPAGENAME",
            "ARTICLESPACE",
            "BASEPAGENAME",
            "FULLPAGENAME",
            "NAMESPACE",
            "PAGENAME",
            "SUBJECTPAGENAME",
            "SUBJECTSPACE",
            "SUBPAGENAME",
            "TALKPAGENAME",
            "TALKSPACE",
            "FULLPAGENAMEE",
            "NAMESPACEE",
            "SITENAME",
            "SERVER",
            "SERVERNAME",
            "SCRIPTPATH",
            "CURRENTVERSION",
            "REVISIONID",
            "REVISIONDAY",
            "REVISIONDAY2",
            "REVISIONMONTH",
            "REVISIONYEAR",
            "REVISIONTIMESTAMP",
            "REVISIONUSER",
            "CURRENTYEAR",
            "CURRENTMONTH",
            "CURRENTMONTHNAME",
            "CURRENTMONTHABBREV",
            "CURRENTDAY",
            "CURRENTDAY2",
            "CURRENTDOW",
            "CURRENTDAYNAME",
            "CURRENTTIME",
            "CURRENTHOUR",
            "CURRENTWEEK",
            "CURRENTTIMESTAMP",
            "LOCALYEAR",
            "NUMBEROFPAGES",
            "NUMBEROFARTICLES",
            "NUMBEROFFILES",
            "NUMBEROFEDITS",
            "NUMBEROFVIEWS",
            "NUMBEROFUSERS",
            "NUMBEROFADMINS",
            "NUMBEROFACTIVEUSERS")));

    public static final Set<String> METADATA = Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(
            "PAGEID",
            "PAGESIZE:",
            "PROTECTIONLEVEL:",
            "PENDINGCHANGELEVEL",
            "PAGESINCATEGORY:",
            "NUMBERINGROUP:"
    )));

    public static final List<String> NAMESPACES = Collections.unmodifiableList(Arrays.asList(
            "Talk",
            "User",
            "User talk",
            "Wikipedia",
            "Wikipedia talk",
            "File",
            "File talk",
            "MediaWiki",
            "MediaWiki talk",
            "Template",
            "Template talk",
            "Help",
            "Help talk",
            "Category",
            "Category talk"
    ));

    private final Map<String, List<TemplateCallHandler>> handlers = new HashMap<>();

    public DefaultTemplateProcessor() {}

    public void process(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateProcessorException {
        try {
            final String name = context.evaluate(call.getName());
            final int splitIndex = name.indexOf(':');
            final String[] nameParts = splitIndex == -1 ?
                    new String[]{name}
                    :
                    new String[] {name.substring(0, splitIndex), name.substring(splitIndex + 1)};
            if (processVariable(name, writer)) {
            } else if (processMetadata(name, writer)) {
            } else if (processFormatting(name, nameParts, context, call, writer)) {
            } else if (processPath(name, nameParts, context, call, writer)) {
            } else if (processConditionalExp(name, nameParts, context, call, writer)) {
            } else if (processHandlers(context, call, writer)) {
            } else {
                throw new TemplateProcessorException("Cannot find handler for template call.", call);
            }
        } catch (Exception e) {
            throw new TemplateProcessorException("Error while processing template call.", e, call);
        }
    }

    @Override
    public void addTemplateCallHandler(String scope, TemplateCallHandler handler) {
        List<TemplateCallHandler> list = handlers.get(scope);
        if(list == null) {
            list = new ArrayList<>();
            handlers.put(scope, list);
        }
        list.add(handler);
    }

    @Override
    public void removeTemplateCallHandler(String scope, TemplateCallHandler handler) {
        List<TemplateCallHandler> list = handlers.get(scope);
        if(list != null) list.remove(handler);
    }

    private List<TemplateCallHandler> getHandlers(String scope) {
        final List<TemplateCallHandler> result = new ArrayList<>(handlers.get(null));
        result.addAll( handlers.get(scope) );
        return result;
    }

    private boolean processVariable(String candidate, HTMLWriter writer) throws IOException {
        if(VARIABLES.contains(candidate)) {
            writer.text(String.format("(variable %s)", candidate));
            return true;
        }
        return false;
    }

    private boolean processMetadata(String candidate, HTMLWriter writer) throws IOException {
        for(String metadata : METADATA) {
            if(candidate.startsWith(metadata)) {
                writer.text(String.format("(metadata %s)", candidate));
                return true;
            }
        }
        return false;
    }

    private boolean processFormatting(
            String name, String[] nameParts, EvaluationContext context, TemplateCall call, HTMLWriter writer
    ) throws IOException, TemplateProcessorException, NodeRenderException {
        if (nameParts.length != 2) return false;
        if (name.startsWith("lc:")) {
            writer.text(nameParts[1].toLowerCase());
            return true;
        }
        if (name.startsWith("lcfirst:")) {
            writer.text(nameParts[1].substring(0, 1).toLowerCase());
            writer.text(nameParts[1].substring(1));
            return true;
        }
        if (name.startsWith("uc:")) {
            writer.text(nameParts[1].toUpperCase());
            return true;
        }
        if (name.startsWith("ucfirst:")) {
            writer.text(nameParts[1].substring(0, 1).toUpperCase());
            writer.text(nameParts[1].substring(1));
            return true;
        }
        if (name.startsWith("formatnum:")) {
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("#formatdate:")) {
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("padleft:")) {
            final int pad = Integer.parseInt(context.evaluate(call.getParameter(0))) - nameParts[1].length();
            writer.text(nameParts[1]);
            for(int i = 0; i < pad; i++) writer.text("0");
            return true;
        }
        if (name.startsWith("padright:")) {
            final int pad = Integer.parseInt(context.evaluate(call.getParameter(0))) - nameParts[1].length();
            for(int i = 0; i < pad; i++) writer.text("0");
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("plural:")) {
            final int count = Integer.parseInt(nameParts[1]);
            writer.text( context.evaluate(call.getParameter(count == 1 ? 0 : 1)) );
            return true;
        }
        if (name.startsWith("#time:")) {
            writer.text( context.evaluate(call.getParameter(0)) );
            return true;
        }
        if (name.startsWith("gender:")) {
            writer.text(String.format(
                    "%s/%s/%s",
                    context.evaluate(call.getParameter(0)),
                    context.evaluate(call.getParameter(1)),
                    context.evaluate(call.getParameter(2))
            ));
            return true;
        }
        if (name.startsWith("#tag:")) {
            writer.openTag(nameParts[1], context.evaluate(call.getParameters(), 1));
            writer.text(context.evaluate(call.getParameter(0)));
            writer.closeTag();
            return true;
        }
        return false;
    }

    private boolean processPath(
            String name, String[] nameParts, EvaluationContext context, TemplateCall call, HTMLWriter writer
    ) throws IOException, TemplateProcessorException, NodeRenderException {
        if (nameParts.length != 2) return false;
        if (name.startsWith("localurl:")) {
            final String query = context.evaluate(call.getParameter(0));
            writer.text(String.format("/w/index.php?title=%s&%s", nameParts[1], query == null ? "" : query));
            return true;
        }
        if (name.startsWith("fullurl:")) {
            final String query = context.evaluate(call.getParameter(0));
            writer.text(getURL(null, null, nameParts[1], query));
            return true;
        }
        if (name.startsWith("canonicalurl:")) {
            final String query = context.evaluate(call.getParameter(0));
            writer.text(getURL("http:", context.getJsonContext().getDomain(), nameParts[1], query));
            return true;
        }
        if (name.startsWith("filepath:")) {
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("urlencode:")) {
            final String format = context.evaluate(call.getParameter(0));
            switch (format) {
                case "WIKI":
                    writer.text(URLEncoder.encode(nameParts[1], "UTF-8"));
                    break;
                case "PATH":
                    writer.text(nameParts[1].replaceAll(" ", "_"));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid format: " + format);
            }
            return true;
        }
        if (name.startsWith("anchorencode:")) {
            writer.text(URLEncoder.encode(nameParts[1], "UTF-8"));
            return true;
        }
        if (name.startsWith("ns:")) {
            final int index = Integer.parseInt(nameParts[1]) - 1;
            writer.text(NAMESPACES.get(index));
            return true;
        }
        if (name.startsWith("#reltoabs:")) {
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("#titleparts:")) {
            writer.text(nameParts[1]);
            return true;
        }
        return false;
    }

    // TODO: some magic words have been not implemented.
    private boolean processConditionalExp(
            String name, String[] nameParts, EvaluationContext context, TemplateCall call, HTMLWriter writer
    ) throws IOException, TemplateProcessorException, NodeRenderException {
        if (name.startsWith("#expr:")) {
            writer.text(nameParts[1]);
            return true;
        }
        if (name.startsWith("#if:")) {
            final String testString = nameParts.length == 1 ? "" : nameParts[1];
            writer.text(context.evaluate(call.getParameter(testString.length() > 0 ? 0 : 1)));
            return true;
        }
        if (name.startsWith("#ifeq:")) {
            final String testString1 = nameParts[1];
            final String testString2 = context.evaluate(call.getParameter(0));
            writer.text(context.evaluate(call.getParameter(testString1.equals(testString2) ? 1 : 2)));
            return true;
        }
        if (name.startsWith("#iferror:")) {
            return true;
        }
        if (name.startsWith("#ifexpr:")) {
            return true;
        }
        if (name.startsWith("#ifexist:")) {
            return true;
        }
        if (name.startsWith("#switch:")) {
            final String testString = nameParts[1];
            final JsonNode matchedCase = call.getParameter(testString);
            if(matchedCase == null) {
                writer.text(context.evaluate(call.getParameter(call.getParametersCount() - 1)));
            } else {
                writer.text(context.evaluate(matchedCase));
            }
            return true;
        }
        return false;
    }

    private boolean processHandlers(EvaluationContext context, TemplateCall call, HTMLWriter writer)
    throws TemplateCallHandlerException {
        for(TemplateCallHandler handler : getHandlers(context.getJsonContext().getDocumentContext().getScope())) {
            if(handler.process(context, call, writer))
                return true;
        }
        return false;
    }

    private String getURL(String protocol, String host, String title, String query) {
        return String.format(
                "%s//%s/w/index.php?title=%s&%s",
                protocol == null ? "" : protocol,
                host == null ? "" : host,
                title == null ? "" : title,
                query == null ? "" : query
        );
    }

}
