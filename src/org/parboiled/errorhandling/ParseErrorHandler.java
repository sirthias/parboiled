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

package org.parboiled.errorhandling;

import org.parboiled.MatcherContext;

/**
 * <p>A handler for parse errors that can be passed to
 * {@link org.parboiled.BaseParser#parse(org.parboiled.Rule, String, ParseErrorHandler)} in order to run custom logic
 * in the event of parse errors.</p>
 * <p>Parboiled comes with three default implementations: {@link NopParseErrorHandler},
 * {@link ReportFirstParseErrorHandler} and {@link RecoveringParseErrorHandler}</p>
 *
 * @param <V>
 */
public interface ParseErrorHandler<V> {

    /**
     * Called before this handler is (re)used for a parsing run.
     * Most handlers will want to reinitialize to the same state as after the first instantiation.
     */
    void initialize();

    /**
     * Called before an actual parsing run or parsing rerun (see {@link #isRerunRequested(org.parboiled.MatcherContext)}
     * on the given root context.
     *
     * @param rootContext the context for the root rule
     */
    void initializeBeforeParsingRerun(MatcherContext<V> rootContext);

    /**
     * <p>This method is being called by the parboiled parser every time a rule match was attempted and succeeded.
     * Since this is the case many times during a parsing run this method should be fast if parsing performance is
     * of the essence.</p>
     *
     * @param context the context whose matcher just matched successfully
     */
    void handleMatch(MatcherContext<V> context);

    /**
     * <p>This method is being called by the parboiled parser every time a rule match was attempted and failed.</p>
     * <p>The method can return true to "overrule" the mismatch and tell the parser to continue parsing as if the
     * contexts matcher had matched</p>
     * <p>Since this is the case many times during a parsing run this method should be fast if parsing performance is
     * of the essence.</p>
     *
     * @param context the context whose matcher just matched failed
     * @return true to "overrule" the mismatch and still match
     */
    boolean handleMismatch(MatcherContext<V> context);

    /**
     * Called by the parser if the root match did not succeed. If the handler returns true the parser will
     * reperform the complete parsing run from the beginning.
     *
     * @param rootContext the root context
     * @return true to redo the parsing run.
     */
    boolean isRerunRequested(MatcherContext<V> rootContext);

}
