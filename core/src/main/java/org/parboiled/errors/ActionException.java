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
 * Exception that can be thrown by parser actions to signal that an error has occurred that is to be collected
 * in the ParseErrors for the parsing run. Throwing an ActionException does not stop the parsing process.
 */
public class ActionException extends RuntimeException {

    public ActionException() {
    }

    public ActionException(String message) {
        super(message);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionException(Throwable cause, String message, Object... messageArgs) {
        super(String.format(message, messageArgs), cause);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }

}