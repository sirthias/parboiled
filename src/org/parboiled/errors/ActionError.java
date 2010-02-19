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

package org.parboiled.errors;

import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

public class ActionError<V> extends SimpleParseError {

    private final MatcherPath<V> errorPath;
    private final ActionException actionException;

    public ActionError(InputLocation errorLocation, String errorMessage, MatcherPath<V> errorPath,
                       ActionException actionException) {
        super(errorLocation, errorMessage);
        this.errorPath = errorPath;
        this.actionException = actionException;
    }

    public MatcherPath<V> getErrorPath() {
        return errorPath;
    }

    public ActionException getActionException() {
        return actionException;
    }

}
