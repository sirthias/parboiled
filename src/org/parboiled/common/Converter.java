/*
 * Copyright (C) 2009 Mathias Doenitz
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

package org.parboiled.common;

/**
 * A simple From-String-Converter that can parse a simple object from a string.
 *
 * @param <T>
 */
public interface Converter<T> {

    /**
     * Parses the given string into an object of type T.
     *
     * @param string the string to parse
     * @return the parse object
     */
    T parse(String string);

}
