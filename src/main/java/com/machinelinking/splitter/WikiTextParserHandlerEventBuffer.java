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

import com.machinelinking.pagestruct.WikiTextHRDumperHandler;
import com.machinelinking.pagestruct.WikiTextSerializerHandler;
import com.machinelinking.pagestruct.WikiTextSerializerHandlerFactory;
import com.machinelinking.parser.WikiTextParserHandler;
import com.machinelinking.serializer.Serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Buffer able to store {@link WikiTextParserHandler} event streams.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserHandlerEventBuffer {

    private final WikiTextParserHandler proxyHandler;

    private final List<Invocation> buffer = new ArrayList<>();

    public WikiTextParserHandlerEventBuffer() {
        proxyHandler = (WikiTextParserHandler) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{WikiTextParserHandler.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        buffer.add( new Invocation(method, args) );
                        return null;
                    }
                }
        );
    }

    public WikiTextParserHandler getProxy() {
        return proxyHandler;
    }

    public void flush(WikiTextParserHandler out) {
        try {
            for (Invocation invocation : buffer) {
                invocation.apply(out);
            }
        } catch (Exception e) {
            final StringBuilder errorSB = new StringBuilder();
            errorSB.append("Error while flushing buffer.\n");
            errorSB.append( e.getMessage() ).append('\n');
            errorSB.append("Content:\n============{\n");
            final WikiTextHRDumperHandler dumper = new WikiTextHRDumperHandler(false);
            flush(dumper);
            errorSB.append( dumper.getContent() );
            errorSB.append("\n}============\n");
            throw new RuntimeException(
                    "Error while flushing buffer into handler.\n" + errorSB.toString(),
                    e
            );
        }
        clear();
    }

    public void flush(Serializer serializer) {
        final WikiTextSerializerHandler handler =
                WikiTextSerializerHandlerFactory.getInstance().createSerializerHandler(serializer);
        flush(handler);
        handler.flush();
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }

    class Invocation {
        private final Method method;
        private final Object[] args;

        Invocation(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        void apply(WikiTextParserHandler out) {
            try {
                method.invoke(out, args);
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format(
                                "Error while applying method %s with args [%s]",
                                method,
                                Arrays.toString(args)
                        ), e
                );
            }
        }
    }

}
