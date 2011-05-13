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

import org.parboiled.common.Formatter;

/**
 * A simple Formatter falling back to the objects toString() method.
 *
 * @param <T>
 */
public class ToStringFormatter<T> implements Formatter<T> {

    private final String nullString;

    public ToStringFormatter() {
        this("null");
    }

    public ToStringFormatter(String nullString) {
        this.nullString = nullString;
    }

    public String format(T obj) {
        return obj != null ? obj.toString() : nullString;
    }

}
