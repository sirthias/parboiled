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

package org.parboiled.support;

/**
 * Defines special meaning characters that are mapped to reserved Unicode non-characters
 * (guaranteed to never actually denote a real unicode char).
 */
public class Chars {

    private Chars() {}

    /**
     * End Of Input
     */
    public static final char EOI = '\uFFFF';

    /**
     * The empty char.
     */
    public static final char EMPTY = '\uFFFE';

    /**
     * any character except {@link Chars#EOI}
     */
    public static final char ANY = '\uFDEF';

    /**
     * Determines wheter c is a special char
     * @param c the char
     * @return true if c is special
     */
    public static boolean isSpecial(char c) {
        return c == EOI || c == ANY || c == EMPTY;
    }

}
