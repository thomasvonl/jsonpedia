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

package com.machinelinking.splitter;

import com.machinelinking.parser.WikiTextParserHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Redirects all the received {@link WikiTextParserHandler}
 * events from a specified nesting level ahead to another {@link WikiTextParserHandler}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserHandlerSplitter {

    private final WikiTextParserHandler proxySplitter;

    private final Stack<String> path = new Stack<>();

    private final List<Redirect> redirections                = new ArrayList<>();
    private final Map<String,Redirect> completedRedirections = new HashMap<>();

    private Method   latestMethod;
    private Object[] latestArgs;

    public WikiTextParserHandlerSplitter() {
        proxySplitter = (WikiTextParserHandler) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{WikiTextParserHandler.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        updateStack(method);
                        handleRedirects(method, args);
                        return null;
                    }
                }
        );
    }

    public void addRedirection(String id, int level, WikiTextParserHandler handler) {
        redirections.add(new Redirect(id, level, handler));
        handleLatest();
    }

    public void addRedirection(String id, WikiTextParserHandler handler) {
        addRedirection(id, getLevel(), handler);
    }

    public WikiTextParserHandlerEventBuffer createRedirection(String id) {
        final WikiTextParserHandlerEventBuffer buffer = new WikiTextParserHandlerEventBuffer();
        addRedirection(id, buffer.getProxy() );
        return buffer;
    }

    public List<Redirect> getActiveRedirections() {
        return Collections.unmodifiableList(redirections);
    }

    public Map<String,Redirect> getcompletedRedirections() {
        return Collections.unmodifiableMap(completedRedirections);
    }

    public void redirectCompleted(Redirect redirect) {
        // Empty.
    }

    public WikiTextParserHandler getProxy() {
        return proxySplitter;
    }

    public int getLevel() {
        return path.size();
    }

    private void updateStack(Method method) {
        final WikiTextParserHandler.Push pushAnnotation = method.getAnnotation(WikiTextParserHandler.Push.class);
        if(pushAnnotation != null) {
            push(pushAnnotation.node());
        } else {
            final WikiTextParserHandler.Pop popAnnotation = method.getAnnotation(WikiTextParserHandler.Pop.class);
            if(popAnnotation != null) pop(popAnnotation.node());
        }
    }

    private void push(String node) {
        path.push(node);
    }

    private void pop(String node) {
        final String peek = path.pop();
        if( ! peek.equals(node))
            throw new IllegalStateException();
    }

    private void handleRedirects(Method method, Object[] args) {
        latestMethod = method;
        latestArgs   = args;

        final Iterator<Redirect> redirectsIterator = redirections.iterator();
        Redirect redirect;
        while(redirectsIterator.hasNext()) {
            redirect = redirectsIterator.next();
            if(path.size() >= redirect.level) {
                try {
                    method.invoke( redirect.handler, args );
                } catch (Exception e) {
                    throw new RuntimeException("Error while redirecting message.", e);
                }
            } else {
                try {
                    latestMethod.invoke( redirect.handler, latestArgs );
                } catch (Exception e) {
                    throw new RuntimeException("Error while closing redirect.", e);
                }
                redirectsIterator.remove();
                completedRedirections.put(redirect.id, redirect);
                redirectCompleted(redirect);
            }
        }
    }

    private void handleLatest() {
        handleRedirects(latestMethod, latestArgs);
    }

    public class Redirect {
        final String id;
        final int level;
        final WikiTextParserHandler handler;

        private Redirect(String name, int level, WikiTextParserHandler handler) {
            this.id      = name;
            this.level   = level;
            this.handler = handler;
        }
    }

}
