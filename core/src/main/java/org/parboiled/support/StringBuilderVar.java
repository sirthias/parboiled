/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.support;

import static org.parboiled.common.Preconditions.checkNotNull;

/**
 * Simple specialization of a {@link org.parboiled.support.Var} for StringBuilders.
 * Provides a few convenience helper methods.
 */
public class StringBuilderVar extends Var<StringBuilder> {

    /**
     * Initializes a new StringVar with a null initial value.
     */
    public StringBuilderVar() {
    }

    /**
     * Initializes a new StringBuilderVar with the given initial string.
     *
     * @param value the initial value
     */
    public StringBuilderVar(String value) {
        super(new StringBuilder(value));
    }
    
    /**
     * Initializes a new StringBuilderVar with the given initial StringBuilder instance.
     *
     * @param value the initial value
     */
    public StringBuilderVar(StringBuilder value) {
        super(value);
    }

    /**
     * Returns true if the wrapped string is either null or empty.
     *
     * @return true if the wrapped string is either null or empty
     */
    public boolean isEmpty() {
        return get() == null || get().length() == 0;
    }

    /**
     * @return the String representation of the underlying StringBuilder.
     */
    public String getString() {
        return checkedGet().toString();
    }
    
    /**
     * @return the char[] representation of the underlying StringBuilder.
     */
    public char[] getChars() {
        StringBuilder sb = checkedGet();
        char[] buf = new char[sb.length()];
        sb.getChars(0, buf.length, buf, 0);
        return buf;
    }

    /**
     * Appends the given string.
     *
     * @param text the text to append
     * @return true
     */
    public boolean append(String text) {
        checkedGet().append(text);
        return true;
    }

    /**
     * Appends the given string.
     *
     * @param text the text to append
     * @return this instance
     */
    public StringBuilderVar appended(String text) {
        checkedGet().append(text);
        return this;
    }

    /**
     * Appends the given char.
     *
     * @param c the char to append
     * @return true
     */
    public boolean append(char c) {
        checkedGet().append(c);
        return true;
    }

    /**
     * Appends the given char.
     *
     * @param c the char to append
     * @return this instance
     */
    public StringBuilderVar appended(char c) {
        checkedGet().append(c);
        return this;
    }

    /**
     * Appends the given strings.
     *
     * @param text1 the first text to append
     * @param text2 the second text to append
     * @return true
     */
    public boolean append(String text1, String text2) {
        checkedGet().append(text1).append(text2);
        return true;
    }

    /**
     * Appends the given strings.
     *
     * @param text1 the first text to append
     * @param text2 the second text to append
     * @return this instance
     */
    public StringBuilderVar appended(String text1, String text2) {
        checkedGet().append(text1).append(text2);
        return this;
    }

    private StringBuilder checkedGet() {
        StringBuilder value = get();
        checkNotNull(value, "Cannot append to a null StringBuilder");
        return value;
    }
}

