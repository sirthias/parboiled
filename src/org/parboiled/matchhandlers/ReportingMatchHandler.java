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

package org.parboiled.matchhandlers;

import com.google.common.base.Supplier;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.common.Formatter;
import org.parboiled.errors.InvalidInputError;

/**
 * A {@link org.parboiled.MatchHandler} that reports the first parse error if the input does not conform to the rule grammar.
 * It initiates at most one parsing rerun (in the case that the input is invalid) and is only a few percent slower
 * than the {@link BasicMatchHandler} on valid input. It is therefore the default {@link org.parboiled.MatchHandler} used by
 * {@link org.parboiled.BaseParser#parse(org.parboiled.Rule, String)}.
 *
 * @param <V>
 */
public class ReportingMatchHandler<V> extends DelegatingMatchHandler<V> {

    private final RecordingMatchHandler<V> recordingHandler = new RecordingMatchHandler<V>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;

    public ReportingMatchHandler() {
        this(new DefaultInvalidInputErrorFormatter<V>());
    }

    public ReportingMatchHandler(@NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextSupplier) {
        if (matchRoot(recordingHandler, rootContextSupplier)) {
            return true;
        }

        ReportFromLocationMatchHandler<V> reportingHandler =
                new ReportFromLocationMatchHandler<V>(recordingHandler.getErrorLocation(), invalidInputErrorFormatter);

        return matchRoot(reportingHandler, rootContextSupplier);
    }

}
