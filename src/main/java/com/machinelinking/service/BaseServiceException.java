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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Base service exception definition.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class BaseServiceException extends WebApplicationException {

    BaseServiceException(Response r) {
        super(r);
    }

    @XmlRootElement
    public static class ExceptionWrapper {
        private final Exception e;

        public ExceptionWrapper(Exception e) {
            this.e = e;
        }

        private ExceptionWrapper() {
            this(null);
        }

        @XmlElement
        public boolean getSuccess() {
            return false;
        }

        @XmlElement
        public String getMessage() {
            return e.getMessage();
        }

        @XmlElement
        public String getStrackTrace() {
            final StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            return stack.toString();
        }
    }

}
