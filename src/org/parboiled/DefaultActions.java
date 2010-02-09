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

import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.EmptyMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.Characters;
import org.parboiled.support.Checks;
import org.parboiled.support.ParsingState;

public class DefaultActions {

    public static final Action checkRecoveryContextAlreadyMatchedSomething =
            new NamedAction("checkRecoveryContextAlreadyMatchedSomething") {
                public boolean run(Context context) {
                    Checks.ensure(context.getParsingState() == ParsingState.Recovering,
                            "checkRecoveryContextAlreadyMatchedSomething action can only be used in parse error recovery");

                    Context recoveryContext = context.getCurrentRecoveryContext();
                    return recoveryContext.getCurrentLocation() != recoveryContext.getStartLocation();
                }
            };

    public static final Action rematchFailedMatcher =
            new NamedAction("rematchFailedMatcher") {
                @SuppressWarnings({"unchecked"})
                public boolean run(Context context) {
                    Checks.ensure(context.getParsingState() == ParsingState.Recovering,
                            "rematchFailedMatcher action can only be used in parse error recovery");

                    Context recoveryContext = context.getCurrentRecoveryContext();
                    Matcher failedMatcher = recoveryContext.getFailedMatcher();
                    MatcherContext matcherContext = (MatcherContext) context;
                    return matcherContext.getSubContext(failedMatcher).runMatcher();
                }
            };

    public static final Action insertCharAndRetry =
            new NamedAction("insertCharAndRetry") {
                @SuppressWarnings({"unchecked"})
                public boolean run(Context context) {
                    Checks.ensure(context.getParsingState() == ParsingState.Recovering,
                            "insertCharAndRetry action can only be used in parse error recovery");

                    Context recoveryContext = context.getCurrentRecoveryContext();
                    Matcher failedMatcher = recoveryContext.getFailedMatcher();
                    Characters starterChars = failedMatcher.getStarterChars();
                    MatcherContext matcherContext = (MatcherContext) context;

                    // we can only insert chars if we can enumerate them
                    // this doesn't work if the starterset is subtractive
                    if (!starterChars.isSubtractive()) {
                        for (char c : starterChars.getChars()) {
                            // inject the character
                            context.injectVirtualInput(c);

                            // rematch the failed matcher
                            if (matcherContext.getSubContext(failedMatcher).runMatcher()) return true;
                        }
                    }
                    return false;
                }
            };

    public static Action isNextCharRecoveryFollower =
            new NamedAction("isNextCharRecoveryFollower") {
                public boolean run(Context context) {
                    Checks.ensure(context.getParsingState() == ParsingState.Recovering,
                            "isNextCharRecoveryFollower action can only be used in parse error recovery");

                    MatcherContext recoveryContext = (MatcherContext) context.getCurrentRecoveryContext();
                    Characters followerChars = recoveryContext.getCurrentFollowerChars();
                    return followerChars.contains(context.getCurrentLocation().currentChar);
                }
            };

    public static Action createEmptyNodeForFailedMatcher =
            new NamedAction("createEmptyNodeForFailedMatcher") {
                @SuppressWarnings({"unchecked"})
                public boolean run(Context context) {
                    Checks.ensure(context.getParsingState() == ParsingState.Recovering,
                            "createEmptyNodeForFailedMatcher action can only be used in parse error recovery");

                    MatcherContext recoveryContext = (MatcherContext) context.getCurrentRecoveryContext();
                    Matcher failedMatcher = recoveryContext.getFailedMatcher();
                    AbstractMatcher emptyMatcher = new EmptyMatcher().label(failedMatcher.getLabel());
                    return recoveryContext.getSubContext(emptyMatcher).runMatcher();
                }
            };

}
