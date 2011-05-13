/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.*;
import org.parboiled.matchervisitors.MatcherVisitor;

import java.util.*;

public class ParserStatistics implements MatcherVisitor<ParserStatistics> {

    private final Matcher root;
    private int totalRules;
    private final Set<AnyMatcher> anyMatchers = new HashSet<AnyMatcher>();
    private final Set<CharIgnoreCaseMatcher> charIgnoreCaseMatchers = new HashSet<CharIgnoreCaseMatcher>();
    private final Set<CharMatcher> charMatchers = new HashSet<CharMatcher>();
    private final Set<CustomMatcher> customMatchers = new HashSet<CustomMatcher>();
    private final Set<CharRangeMatcher> charRangeMatchers = new HashSet<CharRangeMatcher>();
    private final Set<AnyOfMatcher> anyOfMatchers = new HashSet<AnyOfMatcher>();
    private final Set<EmptyMatcher> emptyMatchers = new HashSet<EmptyMatcher>();
    private final Set<FirstOfMatcher> firstOfMatchers = new HashSet<FirstOfMatcher>();
    private final Set<FirstOfStringsMatcher> firstOfStringMatchers = new HashSet<FirstOfStringsMatcher>();
    private final Set<NothingMatcher> nothingMatchers = new HashSet<NothingMatcher>();
    private final Set<OneOrMoreMatcher> oneOrMoreMatchers = new HashSet<OneOrMoreMatcher>();
    private final Set<OptionalMatcher> optionalMatchers = new HashSet<OptionalMatcher>();
    private final Set<SequenceMatcher> sequenceMatchers = new HashSet<SequenceMatcher>();
    private final Set<StringMatcher> stringMatchers = new HashSet<StringMatcher>();
    private final Set<TestMatcher> testMatchers = new HashSet<TestMatcher>();
    private final Set<TestNotMatcher> testNotMatchers = new HashSet<TestNotMatcher>();
    private final Set<ZeroOrMoreMatcher> zeroOrMoreMatchers = new HashSet<ZeroOrMoreMatcher>();

    private final Set<Action> actions = new HashSet<Action>();
    private final Set<Class<?>> actionClasses = new HashSet<Class<?>>();
    private final Set<ProxyMatcher> proxyMatchers = new HashSet<ProxyMatcher>();
    private final Set<VarFramingMatcher> varFramingMatchers = new HashSet<VarFramingMatcher>();
    private final Set<MemoMismatchesMatcher> memoMismatchesMatchers = new HashSet<MemoMismatchesMatcher>();

    @SuppressWarnings({"unchecked"})
    public static ParserStatistics generateFor(Rule rule) {
        checkArgNotNull(rule, "rule");
        Matcher matcher = (Matcher) rule;
        return matcher.accept(new ParserStatistics(matcher));
    }

    private ParserStatistics(Matcher root) {
        this.root = root;
        countSpecials(root);
    }

    public Rule getRootRule() {
        return root;
    }

    public int getTotalRules() {
        return totalRules;
    }

    public Set<AnyMatcher> getAnyMatchers() {
        return anyMatchers;
    }

    public Set<CharIgnoreCaseMatcher> getCharIgnoreCaseMatchers() {
        return charIgnoreCaseMatchers;
    }

    public Set<CharMatcher> getCharMatchers() {
        return charMatchers;
    }

    public Set<CustomMatcher> getCustomMatchers() {
        return customMatchers;
    }

    public Set<CharRangeMatcher> getCharRangeMatchers() {
        return charRangeMatchers;
    }

    public Set<AnyOfMatcher> getAnyOfMatchers() {
        return anyOfMatchers;
    }

    public Set<EmptyMatcher> getEmptyMatchers() {
        return emptyMatchers;
    }

    public Set<FirstOfMatcher> getFirstOfMatchers() {
        return firstOfMatchers;
    }

    public Set<FirstOfStringsMatcher> getFirstOfStringMatchers() {
        return firstOfStringMatchers;
    }

    public Set<MemoMismatchesMatcher> getMemoMismatchesMatchers() {
        return memoMismatchesMatchers;
    }

    public Set<NothingMatcher> getNothingMatchers() {
        return nothingMatchers;
    }

    public Set<OneOrMoreMatcher> getOneOrMoreMatchers() {
        return oneOrMoreMatchers;
    }

    public Set<OptionalMatcher> getOptionalMatchers() {
        return optionalMatchers;
    }

    public Set<SequenceMatcher> getSequenceMatchers() {
        return sequenceMatchers;
    }

    public Set<StringMatcher> getStringMatchers() {
        return stringMatchers;
    }

    public Set<TestMatcher> getTestMatchers() {
        return testMatchers;
    }

    public Set<TestNotMatcher> getTestNotMatchers() {
        return testNotMatchers;
    }

    public Set<ZeroOrMoreMatcher> getZeroOrMoreMatchers() {
        return zeroOrMoreMatchers;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public Set<Class<?>> getActionClasses() {
        return actionClasses;
    }

    public Set<ProxyMatcher> getProxyMatchers() {
        return proxyMatchers;
    }

    public Set<VarFramingMatcher> getVarFramingMatchers() {
        return varFramingMatchers;
    }

    // MatcherVisitor interface

    public ParserStatistics visit(ActionMatcher matcher) {
        if (!actions.contains(matcher.action)) {
            totalRules++;
            actions.add(matcher.action);
            actionClasses.add(matcher.action.getClass());
        }
        return this;
    }

    public ParserStatistics visit(AnyMatcher matcher) {
        return visit(matcher, anyMatchers);
    }

    public ParserStatistics visit(CharIgnoreCaseMatcher matcher) {
        return visit(matcher, charIgnoreCaseMatchers);
    }

    public ParserStatistics visit(CharMatcher matcher) {
        return visit(matcher, charMatchers);
    }

    public ParserStatistics visit(CustomMatcher matcher) {
        return visit(matcher, customMatchers);
    }

    public ParserStatistics visit(CharRangeMatcher matcher) {
        return visit(matcher, charRangeMatchers);
    }

    public ParserStatistics visit(AnyOfMatcher matcher) {
        return visit(matcher, anyOfMatchers);
    }

    public ParserStatistics visit(EmptyMatcher matcher) {
        return visit(matcher, emptyMatchers);
    }

    public ParserStatistics visit(FirstOfMatcher matcher) {
        return matcher instanceof FirstOfStringsMatcher ?
                visit((FirstOfStringsMatcher)matcher, firstOfStringMatchers) :
                visit(matcher, firstOfMatchers);
    }

    public ParserStatistics visit(NothingMatcher matcher) {
        return visit(matcher, nothingMatchers);
    }

    public ParserStatistics visit(OneOrMoreMatcher matcher) {
        return visit(matcher, oneOrMoreMatchers);
    }

    public ParserStatistics visit(OptionalMatcher matcher) {
        return visit(matcher, optionalMatchers);
    }

    public ParserStatistics visit(SequenceMatcher matcher) {
        return matcher instanceof StringMatcher ?
                visit((StringMatcher)matcher, stringMatchers) :
                visit(matcher, sequenceMatchers);
    }

    public ParserStatistics visit(TestMatcher matcher) {
        return visit(matcher, testMatchers);
    }

    public ParserStatistics visit(TestNotMatcher matcher) {
        return visit(matcher, testNotMatchers);
    }

    public ParserStatistics visit(ZeroOrMoreMatcher matcher) {
        return visit(matcher, zeroOrMoreMatchers);
    }

    private <M extends Matcher> ParserStatistics visit(M matcher, Set<M> set) {
        if (!set.contains(matcher)) {
            totalRules++;
            set.add(matcher);
            for (Matcher child : matcher.getChildren()) {
                countSpecials(child);
                child.accept(this);
            }
        }
        return this;
    }

    private void countSpecials(Matcher matcher) {
        if (matcher instanceof ProxyMatcher) {
            proxyMatchers.add((ProxyMatcher) matcher);
        } else if (matcher instanceof VarFramingMatcher) {
            varFramingMatchers.add((VarFramingMatcher) matcher);
        } else if (matcher instanceof MemoMismatchesMatcher) {
            memoMismatchesMatchers.add((MemoMismatchesMatcher) matcher);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("Parser statistics for rule '").append(root).append("':\n")
                .append("    Total rules       : ").append(totalRules).append('\n')
                .append("        Actions       : ").append(actions.size()).append('\n')
                .append("        Any           : ").append(anyMatchers.size()).append('\n')
                .append("        CharIgnoreCase: ").append(charIgnoreCaseMatchers.size()).append('\n')
                .append("        Char          : ").append(charMatchers.size()).append('\n')
                .append("        Custom        : ").append(customMatchers.size()).append('\n')
                .append("        CharRange     : ").append(charRangeMatchers.size()).append('\n')
                .append("        AnyOf         : ").append(anyOfMatchers.size()).append('\n')
                .append("        Empty         : ").append(emptyMatchers.size()).append('\n')
                .append("        FirstOf       : ").append(firstOfMatchers.size()).append('\n')
                .append("        FirstOfStrings: ").append(firstOfStringMatchers.size()).append('\n')
                .append("        Nothing       : ").append(nothingMatchers.size()).append('\n')
                .append("        OneOrMore     : ").append(oneOrMoreMatchers.size()).append('\n')
                .append("        Optional      : ").append(optionalMatchers.size()).append('\n')
                .append("        Sequence      : ").append(sequenceMatchers.size()).append('\n')
                .append("        String        : ").append(stringMatchers.size()).append('\n')
                .append("        Test          : ").append(testMatchers.size()).append('\n')
                .append("        TestNot       : ").append(testNotMatchers.size()).append('\n')
                .append("        ZeroOrMore    : ").append(zeroOrMoreMatchers.size()).append('\n')
                .append('\n')
                .append("    Action Classes    : ").append(actionClasses.size()).append('\n')
                .append("    ProxyMatchers     : ").append(proxyMatchers.size()).append('\n')
                .append("    VarFramingMatchers: ").append(varFramingMatchers.size()).append('\n')
                .append("MemoMismatchesMatchers: ").append(memoMismatchesMatchers.size()).append('\n')
                .toString();
    }

    public String printActionClassInstances() {
        StringBuilder sb = new StringBuilder("Action classes and their instances for rule '")
                .append(root).append("':\n");

        for (String line : printActionClassLines()) {
            sb.append("    ").append(line).append('\n');
        }
        return sb.toString();
    }

    private List<String> printActionClassLines() {
        List<String> lines = new ArrayList<String>();
        int anonymous = 0;
        for (Class<?> actionClass : actionClasses) {
            String name = actionClass.getSimpleName();
            if (StringUtils.isEmpty(name)) {
                anonymous++;
            } else {
                lines.add(name + " : " + StringUtils.join(printActionClassInstances(actionClass), ", "));
            }
        }
        Collections.sort(lines);
        if (anonymous > 0) lines.add("and " + anonymous + " anonymous instance(s)");
        return lines;
    }

    private List<String> printActionClassInstances(Class<?> actionClass) {
        List<String> actionNames = new ArrayList<String>();
        for (Action action : actions) {
            if (action.getClass().equals(actionClass)) {
                actionNames.add(action.toString());
            }
        }
        Collections.sort(actionNames);
        return actionNames;
    }

}
