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

package com.machinelinking.util;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * File utility functions.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class FileUtil {

    public static final String FILE_PREFIX = "file://";

    public static String getExtension(String path) {
        final int extIndex = path.lastIndexOf(".");
        return extIndex != -1 ? path.substring(extIndex + 1) : null;
    }

    public static InputStream openDecompressedInputStream(String resource) throws IOException {
        final InputStream resourceInputStream;
        if (resource.startsWith(FILE_PREFIX)) {
            resourceInputStream = new FileInputStream(resource.substring(FILE_PREFIX.length()));
        } else {
            resourceInputStream = FileUtil.class.getResourceAsStream(resource);
        }
        final String ext = getExtension(resource);
        return openDecompressedInputStream(resourceInputStream, ext);
    }

    public static InputStream openDecompressedInputStream(File file) throws IOException {
        final String ext = getExtension(file.getPath());
        return openDecompressedInputStream( new FileInputStream(file), ext );
    }

    public static BufferedInputStream openDecompressedInputStream(InputStream is, String ext) throws IOException {
        final InputStream decompressInputStream;
        switch (ext) {
            case "gz":
                decompressInputStream = new GZIPInputStream(is);
                break;
            case "bz2":
                decompressInputStream = new BZip2CompressorInputStream(is);
                break;
            default:
                throw new IllegalArgumentException("Unsupported extension: " + ext);
        }
        return new BufferedInputStream(decompressInputStream);
    }

    private FileUtil(){}

}
