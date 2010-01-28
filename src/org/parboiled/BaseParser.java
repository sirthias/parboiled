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
import org.parboiled.common.BitField;
import org.parboiled.common.Preconditions;
import static org.parboiled.common.Utils.arrayOf;
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
     * Level counter used for determining the start of a new rule tree construction.
     */
    private static final ThreadLocal<Integer> ruleDefLevel = new ThreadLocal<Integer>();

    /**
     * Runs the given parser rule against the given input string.
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input) {
        return parse(rule, input, Parboiled.NoOptimization);
    }

    /**
     * Runs the given parser rule against the given input string using the given optimization flags.
     * See {@link Parboiled} class for defined optimization flags.
     *
     * @param rule  the rule
     * @param input the input string
     * @param flags flags indicating requested optimizations, see {@link Parboiled} for defined optimization flags
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(@NotNull Rule rule, @NotNull String input, int flags) {
        Matcher<V> matcher = (Matcher<V>) toRule(rule);
        InputBuffer inputBuffer = new InputBuffer(input);
        List<ParseError> parseErrors = new ArrayList<ParseError>();

        // if mismatch memoizaton is requested each InputLocation receives a BitField containing a bit for every rule
        // index, indicating whether the rule with the respective index has already failed at this location
        BitField failedRules = (flags & Parboiled.MemoizeMismatches) == 0 ? null :
                new BitField(matcher.getIndex() + 1);
        InputLocation startLocation = new InputLocation(inputBuffer, failedRules);

        MatcherContext<V> context = new MatcherContext<V>(inputBuffer, startLocation, matcher, parseErrors);

        // run the actual matcher tree
        context.runMatcher();

        return new ParsingResult<V>(context.getNode(), parseErrors, inputBuffer);
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
            case Chars.EOI:
                return eoi();
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
        return Character.isLowerCase(c) != Character.isUpperCase(c) ? new CharIgnoreCaseMatcher(c) : ch(c);
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
    public Rule string(@NotNull String string) {
        if (string.length() == 1) return ch(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            matchers[i] = ch(string.charAt(i));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
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
    public Rule stringIgnoreCase(String string) {
        if (string.length() == 1) return charIgnoreCase(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            matchers[i] = charIgnoreCase(string.charAt(i));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
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
    @Cached
    public Rule firstOf(Object rule, Object rule2, Object... moreRules) {
        return new FirstOfMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules)))).label("firstOf");
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
    @Cached
    public Rule sequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), false).label("sequence");
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
    @Cached
    public Rule enforcedSequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), true).label("enforcedSequence");
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
        return new TestMatcher(toRule(rule), false);
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
        return new TestMatcher(toRule(rule), true);
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
    public Rule eoi() {
        return new CharMatcher<V>(Chars.EOI);
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
    protected Rule fromStringLiteral(String string) {
        return string(string);
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    protected Rule[] toRules(@NotNull Object... objects) {
        Rule[] rules = new Rule[objects.length];

        // we need to process the sub rule objects in reverse order so as to correctly mix in parameters
        // from the parameter stack
        for (int i = objects.length - 1; i >= 0; i--) {
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
    protected Rule toRule(Object obj) {
        if (obj instanceof Rule) return (Rule) obj;
        if (obj instanceof Character) return fromCharLiteral((Character) obj);
        if (obj instanceof String) return fromStringLiteral((String) obj);
        if (obj instanceof Action) return new ActionMatcher((Action) obj);

        throw new ParserRuntimeException("\'" + obj + "\' cannot be automatically converted to a parser Rule");
    }

    /**
     * Internal method. Automatically called before the execution of custom rule definition code.
     */
    protected void _enterRuleDef() {
        Integer level = ruleDefLevel.get();
        if (level == null) {
            // we are just about to start construction of a new rule tree,
            // so initialize the index counter
            AbstractMatcher.nextIndex.set(0);
            level = -1;
        }
        ruleDefLevel.set(level + 1);
    }

    /**
     * Internal method. Automatically called after the execution of custom rule definition code.
     */
    @SuppressWarnings({"ConstantConditions"})
    protected void _exitRuleDef() {
        Integer level = ruleDefLevel.get();
        Preconditions.checkState(level != null);
        ruleDefLevel.set(level == 0 ? null : level - 1);
    }

}
