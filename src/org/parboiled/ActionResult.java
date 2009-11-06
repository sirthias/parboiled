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

package org.parboiled;

/**
 * Marker interface used for the return values of parser action methods. Parser action methods you want to use
 * directly in your parser rule descriptions must return an ActionResult.
 */
public interface ActionResult {

    /**
     * Return value telling the parser to continue parsing the current rule.
     */
    static final ActionResult CONTINUE = new ActionResult() {};

    /**
     * Return value telling the parser to cancel the current rule match
     * (and either backtrack or generate a parse error).
     */
    static final ActionResult CANCEL_MATCH = new ActionResult() {};

}
