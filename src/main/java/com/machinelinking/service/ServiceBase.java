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


import com.machinelinking.util.Probe;
import org.glassfish.grizzly.nio.transport.TCPNIOConnection;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import java.net.InetSocketAddress;

/**
 * Base service class providing throttling functionalities.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public abstract class ServiceBase implements Service {

    public static final String SERVICE_THROTTLING_PROPERTY = "storage.service.query.throttling";

    private static final long throttling;

    private final Probe probe = Probe.getInstance();

    static {
        try {
            throttling = Long.parseLong(
                    ConfigurationManager.getInstance().getProperty(SERVICE_THROTTLING_PROPERTY, "0")
            );
        }catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    String.format("Invalid value for %s, expected long.", SERVICE_THROTTLING_PROPERTY)
            );
        }
    }

    private final Throttler throttler = new InMemoryThrottler(throttling);

    @Context private Request req;

    //NOTE: this solution has been introduced to bypass issue
    //      http://stackoverflow.com/questions/11312578/how-to-determine-remote-ip-address-from-jax-rs-resource
    public void checkQuota() {
        final String ip;
        try {
            final TCPNIOConnection connection =  probe.probePath(req, "entity.inputBuffer.request.connection");
            ip = ((InetSocketAddress) connection.getPeerAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(throttling == 0) return;
        final long wait = throttler.checkAllowed(ip);
        if(wait > 0) throw new QuotaOverflowException(wait);
    }

}
