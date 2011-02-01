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

import static org.parboiled.common.Preconditions.*;

/**
 * Simple specialization of a {@link Var} for Strings. Provides a few convenience helper methods.
 */
public class StringVar extends Var<String> {

    /**
     * Initializes a new StringVar with a null initial value.
     */
    public StringVar() {
    }

    /**
     * Initializes a new StringVar with the given initial value.
     *
     * @param value the initial value
     */
    public StringVar(String value) {
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
     * Appends the given string.
     *
     * @param text the text to append
     * @return true
     */
    public boolean append(String text) {
        return set(checkedGet().concat(text));
    }

    /**
     * Appends the given string.
     *
     * @param text the text to append
     * @return this instance
     */
    public StringVar appended(String text) {
        append(text);
        return this;
    }
    
    /**
     * Appends the given string.
     * If there is currently no String instance set the method will simply set the given string.
     *
     * @param text the text to append
     * @return true
     */
    public boolean safeAppend(String text) {
        return set(get() == null ? text : get().concat(text));
    }

    /**
     * Appends the given string.
     * If there is currently no String instance set the method will simply set the given string.
     *
     * @param text the text to append
     * @return this instance
     */
    public StringVar safeAppended(String text) {
        safeAppend(text);
        return this;
    }

    /**
     * Appends the given char.
     *
     * @param c the char to append
     * @return true
     */
    public boolean append(char c) {
        return set(checkedGet() + c);
    }

    /**
     * Appends the given char.
     *
     * @param c the char to append
     * @return this instance
     */
    public StringVar appended(char c) {
        append(c);
        return this;
    }

    /**
     * Appends the given string.
     * If there is currently no String instance set the method will simply set the given string.
     *
     * @param c the char to append
     * @return true
     */
    public boolean safeAppend(char c) {
        return set(get() == null ? String.valueOf(c) : get() + c);
    }

    /**
     * Appends the given string.
     * If there is currently no String instance set the method will simply set the given string.
     *
     * @param c the char to append
     * @return this instance
     */
    public StringVar safeAppended(char c) {
        safeAppend(c);
        return this;
    }

    private String checkedGet() {
        String value = get();
        checkNotNull(value, "Cannot append to a null String");
        return value;
    }
}

