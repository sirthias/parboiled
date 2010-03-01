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

package org.parboiled;

import org.parboiled.support.ParsingResult;

/**
 * A ParseRunner performs the actual parsing run of a given parser rule on a given input text.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public interface ParseRunner<V> {

    /**
     * Performs the actual parse and creates a corresponding ParsingResult instance.
     *
     * @return the ParsingResult for the run
     */
    ParsingResult<V> run();

}
