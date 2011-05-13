/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import org.parboiled.common.Utils;

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
        return get() == null ? "" : get().toString();
    }
    
    /**
     * @return the char[] representation of the underlying StringBuilder.
     */
    public char[] getChars() {
        if (get() == null) return new char[0];
        StringBuilder sb = get();
        char[] buf = new char[sb.length()];
        sb.getChars(0, buf.length, buf, 0);
        return buf;
    }

    /**
     * Appends the given string.
     * If this instance is currently uninitialized the given string is used for initialization.
     *
     * @param text the text to append
     * @return true
     */
    public boolean append(String text) {
        if (get() == null) return set(new StringBuilder(text));
        get().append(text);
        return true;
    }

    /**
     * Appends the given string.
     * If this instance is currently uninitialized the given string is used for initialization.
     *
     * @param text the text to append
     * @return this instance
     */
    public StringBuilderVar appended(String text) {
        append(text);
        return this;
    }
    
    /**
     * Appends the given char.
     * If this instance is currently uninitialized the given char is used for initialization.
     *
     * @param c the char to append
     * @return true
     */
    public boolean append(char c) {
        if (get() == null) return set(new StringBuilder().append(c));
        get().append(c);
        return true;
    }

    /**
     * Appends the given char.
     * If this instance is currently uninitialized the given char is used for initialization.
     *
     * @param c the char to append
     * @return this instance
     */
    public StringBuilderVar appended(char c) {
        append(c);
        return this;
    }
    
    /**
     * Clears the contents of the wrapped StringBuilder.
     * If the instance is currently unintialized this method does nothing. 
     * @return true
     */
    public boolean clearContents() {
        if (get() != null) get().setLength(0);
        return true;
    }
    
    /**
     * Clears the contents of the wrapped StringBuilder.
     * If the instance is currently unintialized this method does nothing. 
     * @return this instance
     */
    public StringBuilderVar contentsCleared() {
        if (get() != null) get().setLength(0);
        return this;
    }
}

