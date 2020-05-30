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

package com.machinelinking.exporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * <i>CSV</i> data exporter.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public interface CSVExporter {

    /**
     * Sets the number of concurrent threads processing exporter.
     *
     * @param count positive number of threads.
     */
    void setThreads(int count);

    /**
     * Exports a given Wiki page fetched from the specified input stream <code>is</code>
     * to the specified output stream <code>os</code> as <i>CSV</i>.
     *
     * @param pagePrefix
     * @param is
     * @param os
     * @return the report related to the exporting activity.
     * @throws IOException if any error occur while reading or writing.
     */
    CSVExporterReport export(URL pagePrefix, InputStream is, OutputStream os) throws IOException;

    /**
     * Exports a given Wiki page fetched from the specified input file <code>in</code>
     * to the specified output file <code>out</code> as <i>CSV</i>.
     *
     * @param pagePrefix
     * @param in
     * @param out
     * @return the report related to the exporting activity.
     * @throws java.io.IOException if any error occur while reading or writing.
     */
    CSVExporterReport export(URL pagePrefix, File in, File out) throws IOException;

}
