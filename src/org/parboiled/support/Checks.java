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

public class Checks {

    private Checks() {}

    public static void ensure(boolean condition, String errorMessageFormat, Object... errorMessageArgs) {
        if (!condition) {
            fail(errorMessageFormat, errorMessageArgs);
        }
    }

    public static void ensure(boolean condition, String errorMessage) {
        if (!condition) {
            fail(errorMessage);
        }
    }

    public static void fail(String errorMessage) {
        throw new ParserConstructionException(errorMessage);
    }

    public static void fail(String errorMessageFormat, Object... errorMessageArgs) {
        throw new ParserConstructionException(String.format(errorMessageFormat, errorMessageArgs));
    }

}
