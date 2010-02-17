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

import org.jetbrains.annotations.NotNull;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.matchers.*;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

public class DefaultRecoveryRuleVisitorImpl<V> implements DefaultRecoveryRuleVisitor<V> {

    private Context<V> context;
    private MatcherPath<V> lastMatch;
    private InputLocation errorLocation;

    public void setContext(@NotNull Context<V> context) {
        this.context = context;
    }

    public void setLastMatch(MatcherPath<V> lastMatch) {
        this.lastMatch = lastMatch;
    }

    public void setErrorLocation(InputLocation errorLocation) {
        this.errorLocation = errorLocation;
    }

    public Rule visit(ActionMatcher<V> matcher) {
        // never try to recover from action failures
        return null;
    }

    public Rule visit(CharactersMatcher<V> matcher) {
        // try one char deletion and then one char insertion if we are underneatch a sequence
        return defaultSingleCharRecovery();
    }

    public Rule visit(CharIgnoreCaseMatcher<V> matcher) {
        // try one char deletion and then one char insertion if we are underneatch a sequence
        return defaultSingleCharRecovery();
    }

    public Rule visit(CharMatcher<V> matcher) {
        // try one char deletion and then one char insertion if we are underneatch a sequence
        return defaultSingleCharRecovery();
    }

    public Rule visit(CharRangeMatcher<V> matcher) {
        // try one char deletion if we are underneatch a sequence, we don't try single char insertion since
        // conjuring up a digit for example is not very helpful in most cases
        return defaultSingleCharDeletion();
    }

    public Rule visit(EmptyMatcher<V> matcher) {
        throw new IllegalStateException(); // EmptyMatchers should never cause a mismatch
    }

    public Rule visit(FirstOfMatcher<V> matcher) {
        // try one char deletion if we are underneatch a sequence
        return defaultSingleCharDeletion();
    }

    public Rule visit(OneOrMoreMatcher<V> matcher) {
        // try one char deletion if we are underneatch a sequence
        return defaultSingleCharDeletion();
    }

    public Rule visit(OptionalMatcher<V> matcher) {
        throw new IllegalStateException(); // OptionalMatchers should never cause a mismatch
    }

    public Rule visit(SequenceMatcher<V> matcher) {
        // sequences are the only rules we use resynchronization on
        // however, we only resync if the sequence qualifies as a resynchronization sequence
        return isResynchronizationSequence(matcher) ?
                context.getParser().resynchronize(context, errorLocation) : null;
    }

    public Rule visit(TestMatcher<V> matcher) {
        return defaultSingleCharDeletion();
    }

    public Rule visit(TestNotMatcher<V> matcher) {
        return defaultSingleCharDeletion();
    }

    public Rule visit(ZeroOrMoreMatcher<V> matcher) {
        throw new IllegalStateException(); // ZeroOrMoreMatchers should never cause a mismatch
    }

    // ****************** PRIVATE ***********************

    private Rule defaultSingleCharRecovery() {
        return context.getCurrentLocation().index == errorLocation.index &&
                getParentMatcher() instanceof SequenceMatcher ?
                context.getParser().singleCharRecovery(context) : null;
    }

    private Rule defaultSingleCharDeletion() {
        return context.getCurrentLocation().index == errorLocation.index &&
                getParentMatcher() instanceof SequenceMatcher ?
                context.getParser().skipCharRecovery(context.getMatcher()) : null;
    }

    private Matcher<V> getParentMatcher() {
        return context.getParent() != null ? context.getParent().getMatcher() : null;
    }

    private boolean isResynchronizationSequence(Matcher<V> matcher) {
        // never resync on "helper" sequences without a "real" name
        if ("sequence".equals(matcher.getLabel())) return false;

        // don't resync if we are not a parent sequence of the last match
        if (!context.getPath().isPrefixOf(lastMatch)) return false;

        // also dont resync if the sequence is actually "not really" a sequence since it only has less than two
        // real rule children, helper constructs like test/testNot rules and actions do not qualify as "real children"
        int realChildren = 0;
        for (Matcher<V> child : matcher.getChildren()) {
            if (child instanceof TestMatcher || child instanceof TestNotMatcher || child instanceof ActionMatcher) {
                continue;
            }
            realChildren++;
        }
        return realChildren >= 2;
    }
}