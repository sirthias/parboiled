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

import com.google.common.base.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * <p>A handler for parse errors that can be passed to
 * {@link org.parboiled.BaseParser#parse(org.parboiled.Rule, String, MatchHandler)} in order to run custom logic
 * in the event of parse errors.</p>
 * <p>Parboiled comes with three default implementations: {@link org.parboiled.matchhandlers.BasicMatchHandler},
 * {@link org.parboiled.matchhandlers.ReportFirstMatchHandler} and {@link org.parboiled.matchhandlers.RecoveringMatchHandler}</p>
 *
 * @param <V>
 */
public interface MatchHandler<V> {

    boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextProvider);

    boolean match(MatcherContext<V> context) throws Throwable;

}
