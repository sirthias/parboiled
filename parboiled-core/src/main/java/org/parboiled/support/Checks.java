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

import org.parboiled.errors.GrammarException;

/**
 * Utility methods for grammar integrity checks.
 */
public final class Checks {

    private Checks() {}

    /**
     * Throws a GrammarException if the given condition is not met.
     *
     * @param condition          the condition
     * @param errorMessageFormat the error message format
     * @param errorMessageArgs   the error message arguments
     */
    public static void ensure(boolean condition, String errorMessageFormat, Object... errorMessageArgs) {
        if (!condition) {
            throw new GrammarException(errorMessageFormat, errorMessageArgs);
        }
    }

    /**
     * Throws a GrammarException if the given condition is not met.
     *
     * @param condition    the condition
     * @param errorMessage the error message
     */
    public static void ensure(boolean condition, String errorMessage) {
        if (!condition) {
            throw new GrammarException(errorMessage);
        }
    }

}
