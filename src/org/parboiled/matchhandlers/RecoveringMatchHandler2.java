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
import org.parboiled.support.InputLocation;
import org.parboiled.support.InputBuffer;

public class RecoveringMatchHandler2<V> extends DelegatingMatchHandler<V> {

    public static final char ANY = '\uFFFE';

    private final RecordingMatchHandler<V> recordingHandler = new RecordingMatchHandler<V>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private Supplier<MatcherContext<V>> rootContextSupplier;
    private InputBuffer inputBuffer;

    public RecoveringMatchHandler2() {
        this(new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringMatchHandler2(@NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextSupplier) {
        this.rootContextSupplier = rootContextSupplier;

        if (!attemptRecordingMatch()) {
            inputBuffer = recordingHandler.getLastRootContext().getInputBuffer();
            InputLocation errorLocation = recordingHandler.getErrorLocation();
            do {
                performErrorReportingRun(errorLocation);
                errorLocation = fix(errorLocation);
            } while (errorLocation != null);
        }
        return true;
    }

    private boolean attemptRecordingMatch() {
        return matchRoot(recordingHandler, rootContextSupplier);
    }

    private void performErrorReportingRun(InputLocation errorLocation) {
        ReportFromLocationMatchHandler<V> reportingHandler =
                new ReportFromLocationMatchHandler<V>(errorLocation, invalidInputErrorFormatter);
        matchRoot(reportingHandler, rootContextSupplier);
    }

    private InputLocation fix(InputLocation fixLocation) {
        InputLocation saved = fixLocation.remove(inputBuffer);
        if (attemptRecordingMatch()) {
            return null;
        }
        InputLocation nextErrorAfterDeletion = recordingHandler.getErrorLocation();
        fixLocation.insert(saved);

        fixLocation.insert(ANY);
        if (attemptRecordingMatch()) {
            return null;
        }
        InputLocation nextErrorAfterInsertion = recordingHandler.getErrorLocation();
        fixLocation.remove(inputBuffer); // remove joker char

        // resync
        if (attemptRecordingMatch()) {
            return null;
        }
        InputLocation nextErrorAfterResync = recordingHandler.getErrorLocation();
        // unresync

        if (nextErrorAfterDeletion.getIndex() >= Math.max(nextErrorAfterInsertion.getIndex(), nextErrorAfterResync.getIndex())) {
            fixLocation.remove(inputBuffer);
            return nextErrorAfterDeletion;
        }
        if (nextErrorAfterInsertion.getIndex() >= nextErrorAfterResync.getIndex()) {
            fixLocation.insert(ANY);
            return nextErrorAfterInsertion;
        }

        // resync
        return nextErrorAfterResync;
    }

}
