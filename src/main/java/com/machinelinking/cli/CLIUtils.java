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

package com.machinelinking.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Resources utility for <b>CLI</b> classes.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class CLIUtils {

    public static class FileConverter implements IStringConverter<File> {
        @Override
        public File convert(String value) {
            return new File(value);
        }
    }

    public static class URLConverter implements IStringConverter<URL> {
        @Override
        public URL convert(String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException murle) {
                throw new ParameterException( String.format("Invalid prefix [%s] : must be a URL.", value) );
            }
        }
    }

    public static class ValidURL implements IValueValidator<String> {
        public void validate(String name, String url)
        throws ParameterException {
            try {
                new URL(url);
            } catch (MalformedURLException murle) {
                throw new ParameterException( String.format("Invalid URL [%s]", url) );
            }
        }
    }

    public static class ValidHost implements IValueValidator<String> {
        public void validate(String name, String host)
        throws ParameterException {
            try {
                new URL(String.format("http://%s/", host));
            } catch (MalformedURLException murle) {
                throw new ParameterException( String.format("Invalid host [%s]", host) );
            }
        }
    }

    public static class ExistingFile implements IValueValidator<File> {
        public void validate(String name, File f)
        throws ParameterException {
            if (! f.exists()) {
                throw new ParameterException( String.format("File [%s] must exist.", f.getAbsolutePath()) );
            }
        }
    }

    public static class PortValidator implements IValueValidator<Integer> {
        public void validate(String name, Integer v)
        throws ParameterException {
            if(v < 0 || v > 65535) throw new ParameterException(
                    "Invalid port, must be >= 0 && <= 65535"
            );
        }
    }

    public static class NumOfThreadsValidator implements IValueValidator<Integer> {
        public void validate(String name, Integer v)
        throws ParameterException {
            if(v < 0) throw new ParameterException(
                    "Invalid number of threads, must be >= 0. (0 => NumOfThreads == # CPU Cores)"
            );
        }
    }

}
