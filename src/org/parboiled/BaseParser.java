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

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.Preconditions;
import org.parboiled.common.Reference;
import static org.parboiled.common.Utils.arrayOf;
import static org.parboiled.common.Utils.toObjectArray;
import org.parboiled.exceptions.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for custom parsers. Defines basic methods for rule and action parameter creation.
 *
 * @param <V> The type of the value field of the parse tree nodes created by this parser.
 */
public abstract class BaseParser<V> extends BaseActions<V> {

    /**
     * Runs the given parser rule against the given input string using performing no error recovery.
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input) {
        return parse(rule, input, false);
    }

    /**
     * Runs the given parser rule against the given input string using performing no error recovery.
     *
     * @param rule                   the rule
     * @param input                  the input string
     * @param recoverFromParseErrors flag indicating whether the parser should recover from parse errors
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input, boolean recoverFromParseErrors) {
        return parse(rule, input, recoverFromParseErrors ?
                new RecoveringParseErrorHandler<V>() : new ReportingParseErrorHandler<V>());
    }

    /**
     * Runs the given parser rule against the given input string.
     *
     * @param rule              the rule
     * @param input             the input string
     * @param parseErrorHandler the ParseErrorHandler to use
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input, @NotNull ParseErrorHandler<V> parseErrorHandler) {
        InputBuffer inputBuffer = new InputBuffer(input);
        List<ParseError<V>> parseErrors = new ArrayList<ParseError<V>>();
        Reference<Node<V>> lastNodeRef = new Reference<Node<V>>();
        Matcher<V> matcher = (Matcher<V>) toRule(rule);

        MatcherContext<V> context = new MatcherContext<V>(
                inputBuffer, parseErrors, lastNodeRef, parseErrorHandler, this, matcher
        );
        context.setEnforcement();

        // run the actual matcher tree
        context.runMatcher();

        return new ParsingResult<V>(lastNodeRef.getTarget(), parseErrors, inputBuffer,
                context.getCurrentLocation().row + 1);
    }

    ////////////////////////////////// RULE CREATION ///////////////////////////////////

    /**
     * Explicitly creates a rule matching the given character. Normally you can just specify the character literal
     * directly in you rule description. However, if you don't want to go through {@link #fromCharLiteral(char)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match
     * @return a new rule
     */
    @Cached
    public Rule ch(char c) {
        switch (c) {
            case Chars.EMPTY:
                return empty();
            case Chars.ANY:
                return any();
            default:
                return new CharMatcher(c);
        }
    }

    /**
     * Explicitly creates a rule matching the given character ignoring the case.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match independently of its case
     * @return a new rule
     */
    @Cached
    public Rule charIgnoreCase(char c) {
        return Character.isLowerCase(c) != Character.isUpperCase(c) ?
                new CharIgnoreCaseMatcher(c) : ch(c);
    }

    /**
     * Creates a rule matching a range of characters from cLow to cHigh (both inclusively).
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param cLow  the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a new rule
     */
    @Cached
    public Rule charRange(char cLow, char cHigh) {
        return cLow == cHigh ? ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Creates a new rule that matches the first of the given characters.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @Cached
    @Leaf
    public Rule charSet(@NotNull String characters) {
        Preconditions.checkArgument(characters.length() > 0);
        if (characters.length() == 1) return ch(characters.charAt(0)); // optimize one-char sets
        return firstOf(toObjectArray(characters.toCharArray())).label('{' + characters + '}');
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the string to match
     * @return a new rule
     */
    @Cached
    @Leaf
    public Rule string(@NotNull String string) {
        if (string.length() == 1) return ch(string.charAt(0)); // optimize one-char strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            matchers[i] = ch(string.charAt(i));
        }
        return sequence(matchers).label('"' + string + '"');
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the string to match
     * @return a new rule
     */
    @Cached
    @Leaf
    public Rule stringIgnoreCase(@NotNull String string) {
        if (string.length() == 1) return charIgnoreCase(string.charAt(0)); // optimize one-char strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            matchers[i] = charIgnoreCase(string.charAt(i));
        }
        return sequence(matchers).label('"' + string + '"');
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule firstOf(Object rule, Object rule2, @NotNull Object... moreRules) {
        return firstOf(arrayOf(rule, arrayOf(rule2, moreRules)));
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rules the subrules
     * @return a new rule
     */
    @Cached
    public Rule firstOf(@NotNull Object[] rules) {
        return rules.length == 1 ? toRule(rules[0]) : new FirstOfMatcher(toRules(rules)).label("firstOf");
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule oneOrMore(Object rule) {
        return new OneOrMoreMatcher(toRule(rule)).label("oneOrMore");
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its subrule.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule optional(Object rule) {
        return new OptionalMatcher(toRule(rule)).label("optional");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule sequence(Object rule, Object rule2, @NotNull Object... moreRules) {
        return sequence(arrayOf(rule, arrayOf(rule2, moreRules)));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rules the sub rules
     * @return a new rule
     */
    @Cached
    public Rule sequence(@NotNull Object[] rules) {
        return rules.length == 1 ? toRule(rules[0]) :
                new SequenceMatcher(toRules(rules), false).label("sequence");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrules succeed, one after the other.
     * However, after the first subrule has matched all further subrule matches are enforced, i.e. if one of them
     * fails a ParseError will be created (and error recovery will be tried).
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule enforcedSequence(Object rule, Object rule2, @NotNull Object... moreRules) {
        return enforcedSequence(arrayOf(rule, arrayOf(rule2, moreRules)));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrules succeed, one after the other.
     * However, after the first subrule has matched all further subrule matches are enforced, i.e. if one of them
     * fails a ParseError will be created (and error recovery will be tried).
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rules the sub rules
     * @return a new rule
     */
    @Cached
    public Rule enforcedSequence(@NotNull Object[] rules) {
        return rules.length == 1 ? toRule(rules[0]) :
                new SequenceMatcher(toRules(rules), true).label("enforcedSequence");
    }

    /**
     * Creates a new rule that acts as a syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule succeeds and fails if the
     * subrule rails. Since this rule does not actually consume any input it will never create a parse tree node.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule test(Object rule) {
        return new TestMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule fails and fails if the
     * subrule succeeds. Since this rule does not actually consume any input it will never create a parse tree node.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule testNot(Object rule) {
        return new TestNotMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule zeroOrMore(Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule)).label("zeroOrMore");
    }

    /**
     * Matches the EOI (end of input) character.
     *
     * @return a new rule
     */
    @KeepAsIs
    public Rule eoi() {
        return ch(Chars.EOI);
    }

    /**
     * Matches any character except {@link org.parboiled.support.Chars#EOI}.
     *
     * @return a new rule
     */
    public Rule any() {
        return new AnyCharMatcher<V>();
    }

    /**
     * Matches nothing and therefore always succeeds.
     *
     * @return a new rule
     */
    public Rule empty() {
        return new EmptyMatcher<V>();
    }

    ///************************* "MAGIC" METHODS ***************************///

    /**
     * Changes the context scope of all arguments to the current parent scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the current sub scope. This will only work if this call is
     * at some level wrapped with one or more {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    ///************************* HELPER METHODS ***************************///

    /**
     * Used internally to convert the given character literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the character.
     *
     * @param c the character
     * @return the rule
     */
    protected Rule fromCharLiteral(char c) {
        return ch(c);
    }

    /**
     * Used internally to convert the given string literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the string.
     *
     * @param string the string
     * @return the rule
     */
    protected Rule fromStringLiteral(@NotNull String string) {
        return string(string);
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    public Rule[] toRules(@NotNull Object... objects) {
        Rule[] rules = new Rule[objects.length];
        for (int i = 0; i < objects.length; i++) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    /**
     * Converts the given object to a rule.
     * This method can be overriden to enable the use of custom objects directly in rule specifications.
     *
     * @param obj the object to convert
     * @return the rule corresponding to the given object
     */
    @SuppressWarnings({"unchecked"})
    public Rule toRule(Object obj) {
        if (obj instanceof Rule) return (Rule) obj;
        if (obj instanceof Character) return fromCharLiteral((Character) obj);
        if (obj instanceof String) return fromStringLiteral((String) obj);
        if (obj instanceof Action) return new ActionMatcher<V>((Action<V>) obj);

        throw new ParserRuntimeException("'" + obj + "' cannot be automatically converted to a parser Rule");
    }

    public Rule illegal(Object rule) {
        return toRule(rule).label(Parboiled.ILLEGAL);
    }

    public Rule defaultSingleCharRecoveryRule(Context<V> failedMatcherContext) {
        return firstOf(
                // first test: delete one char and retry
                sequence(
                        // match one character and mark it ILLEGAL
                        illegal(any()),

                        // rerun the failed matcher
                        Actions.match(failedMatcherContext.getMatcher())
                ).withoutNode().label("deleteOne"),

                // second test: insert one char and retry
                sequence(
                        // test whether current mismatching character is a legal follower
                        Actions.isNextCharIn(failedMatcherContext.getCurrentFollowerChars()),

                        // if so, match "empty"
                        Actions.createEmptyNodeFor(failedMatcherContext.getMatcher())
                ).withoutNode().label("insertOne")
        );
    }

    public Rule defaultSequenceRecoveryRule(Context<V> failedMatcherContext) {
        return sequence(
                // we only recover if we have already matched something in this sequence,
                // otherwise just continue bubbling up the parse error through the matcher stack
                Actions.testContextAlreadyMatchedSomething(failedMatcherContext),

                // fallback: resync
                sequence(
                        Actions.createEmptyNodeFor(failedMatcherContext.getMatcher()),
                        zeroOrMore(
                                sequence(
                                        testNot(Actions.isNextCharIn(failedMatcherContext.getCurrentFollowerChars())),
                                        illegal(any())
                                ).withoutNode()
                        ).withoutNode()
                ).withoutNode().label("resync")
        ).withoutNode();
    }

}
