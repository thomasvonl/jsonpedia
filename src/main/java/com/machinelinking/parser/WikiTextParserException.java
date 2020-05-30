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

/**
 * Any exception raised by {@link WikiTextParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WikiTextParserException extends Exception {

    private int row, col;

    public WikiTextParserException(int row, int col, String msg) {
        super(msg);
        this.row = row;
        this.col = col;
    }

    public WikiTextParserException(int row, int col, Throwable e) {
        super(e);
        this.row = row;
        this.col = col;
    }

    public WikiTextParserException(int row, int col, String msg, Throwable e) {
        super(msg, e);
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return String.format("%s (row,col: %d,%d)", super.toString(), getRow(), getCol());
    }

}
