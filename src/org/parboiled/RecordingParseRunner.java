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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParsingResult;

public class RecordingParseRunner<V> extends BasicParseRunner<V> {

    private Handler<V> handler;

    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new RecordingParseRunner<V>(rule, input).run();
    }

    public RecordingParseRunner(@NotNull Rule rule, @NotNull String input) {
        super(rule, input);
    }

    @Override
    protected boolean runRootContext() {
        handler = new Handler<V>();
        return runRootContext(handler);
    }

    public InputLocation getErrorLocation() {
        Preconditions.checkState(handler != null, "getErrorLocation() called before run()");
        return handler.getErrorLocation();
    }

    public static class Handler<V> implements MatchHandler<V> {
        private InputLocation errorLocation;
        private final MatchHandler<V> inner;

        public Handler() {
            this(new BasicParseRunner.Handler<V>());
        }

        public Handler(@NotNull MatchHandler<V> inner) {
            this.inner = inner;
        }

        public InputLocation getErrorLocation() {
            return errorLocation;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            errorLocation = rootContext.getCurrentLocation();
            if (inner.matchRoot(rootContext)) {
                errorLocation = null;
                return true;
            }
            return false;
        }

        public boolean match(MatcherContext<V> context) {
            if (inner.match(context)) {
                if (errorLocation.getIndex() < context.getCurrentLocation().getIndex()) {
                    errorLocation = context.getCurrentLocation();
                }
                return true;
            }
            return false;
        }

    }

}