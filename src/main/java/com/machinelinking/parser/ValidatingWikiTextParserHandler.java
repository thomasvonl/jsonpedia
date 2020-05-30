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

package com.machinelinking.parser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Stack;

/**
 * {@link WikiTextParserHandler} able to validate the event stream.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ValidatingWikiTextParserHandler {

    private final String name;
    private final WikiTextParserHandler wrapped;
    private final WikiTextParserHandler proxyHandler;

    private final Stack<NodeElement> stack = new Stack<NodeElement>();

    public ValidatingWikiTextParserHandler(String name, WikiTextParserHandler w) {
        if(name == null) throw new NullPointerException();
        if(w == null) throw new NullPointerException();
        this.name = name;
        this.wrapped = w;

        proxyHandler = (WikiTextParserHandler) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{WikiTextParserHandler.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        validateCallStack(method, args);
                        method.invoke(wrapped, args);
                        return null;
                    }
                }
        );
    }

    public WikiTextParserHandler getProxy() {
        return proxyHandler;
    }

    private void validateCallStack(Method method, Object[] args) {
        final WikiTextParserHandler.Push push = method.getAnnotation(WikiTextParserHandler.Push.class);
        if(push != null) {
            stack.push( new NodeElement(push.node(), getId(push.id(), args)) );
        } else {
            final WikiTextParserHandler.Pop pop = method.getAnnotation(WikiTextParserHandler.Pop.class);
            if(pop != null) {
                final NodeElement node = new NodeElement(pop.node(), getId(pop.id(), args));
                if(stack.empty()) throw new IllegalStateException(
                        getErrorName() + " Expected element in stack for node " + node
                );
                final NodeElement peek = stack.pop();
                peek.checkMatch(node);
            }
        }
        if(method.getAnnotation(WikiTextParserHandler.ValidateStack.class) != null) {
            if( ! stack.isEmpty() ) {
                throw new IllegalStateException(getErrorName() + " Stack must be empty. Found " + stack.toString() );
            }
        }
    }

    private String getId(int idIndex, Object[] args) {
        if(idIndex == -1) return null;
        final Object arg = args[idIndex];
        return arg == null ? null : arg.toString();
    }

    private String getErrorName() {
        return "Error in validator [" + name + "]";
    }

    class NodeElement {
        final String node;
        final String id;

        NodeElement(String node, String id) {
            if(node == null) throw new IllegalArgumentException();
            this.node = node;
            this.id   = id;
        }

        private void checkMatch(NodeElement other) {
            if(! node.equals(other.node)) throw new IllegalArgumentException(
                    String.format(getErrorName() + " Invalid node, expected: [%s] found [%s]", node, other.node)
            );
            if(id != null && ! id.equals(other.id)) throw new IllegalArgumentException(
                    String.format(getErrorName() + " Invalid node id, expected: [%s] found [%s]", id, other.id)
            );
        }

        @Override
        public String toString() {
            return String.format("{node: [%s], id: [%s]}", node, id);
        }
    }
}
