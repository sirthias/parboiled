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

package org.parboiled.errors;

/**
 * Exception type not directly used by parboiled but included as a convenience base class for custom exceptions
 * (or to be used directly if no custom exception types are required).
 */
public class ParsingException extends RuntimeException {

    public ParsingException() {
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(String message, Object... messageArgs) {
        super(String.format(message, messageArgs));
    }

    public ParsingException(Throwable cause, String message, Object... messageArgs) {
        super(String.format(message, messageArgs), cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }

}