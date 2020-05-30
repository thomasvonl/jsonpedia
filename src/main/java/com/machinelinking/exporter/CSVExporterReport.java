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

import com.machinelinking.wikimedia.ProcessorReport;

/**
 * Execution report for {@link CSVExporter}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class CSVExporterReport {

    private final ProcessorReport processorReport;
    private final long templatesCount;
    private final long propertiesCount;
    private final int  maxPropertiesPerTemplate;


    public CSVExporterReport(
            ProcessorReport processorReport,
            long templatesCount, long propertiesCount, int maxPropertiesPerTemplate
    ) {
        this.processorReport = processorReport;
        this.templatesCount = templatesCount;
        this.propertiesCount = propertiesCount;
        this.maxPropertiesPerTemplate = maxPropertiesPerTemplate;
    }

    public ProcessorReport getProcessorReport() {
        return processorReport;
    }

    public long getTemplatesCount() {
        return templatesCount;
    }

    public long getPropertiesCount() {
        return propertiesCount;
    }

    public int getMaxPropertiesPerTemplate() {
        return maxPropertiesPerTemplate;
    }

    @Override
    public String toString() {
        return String.format(
            "processor: %s\n\ttemplates: %d, properties %d, max properties/template: %d, avg properties/template: %f\n",
            processorReport, templatesCount, propertiesCount, maxPropertiesPerTemplate,
                (float)propertiesCount / templatesCount
        );
    }

}
