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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import org.jetbrains.annotations.NotNull;
import org.parboiled.actionparameters.*;
import static org.parboiled.actionparameters.ActionParameterUtils.mixInParameter;
import org.parboiled.common.Converter;
import org.parboiled.common.Preconditions;
import org.parboiled.common.Utils;
import static org.parboiled.common.Utils.arrayOf;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import java.util.*;

/**
 * Base class for custom parsers. Defines basic methods for rule and action parameter creation.
 *
 * @param <V> The type of the value field of the parse tree nodes created by this parser.
 * @param <A> The type of the parser Actions you would like to use in your rules. If you don't need any parser
 * actions (e.g. for very simple examples) you can just use the Actions<Object> interface directly.
 */
public abstract class BaseParser<V, A extends Actions<V>> {

    /**
     * Cache of frequently used, bottom level rules. Per default used for character and string matching rules.
     */
    private final Map<Object, Rule> ruleCache = new HashMap<Object, Rule>();

    /**
     * The actual type of the V type argument, i.e. the value field of the generated parse tree nodes.
     */
    private final Class<V> nodeValueType;

    /**
     * Stack for action parameters. Used for creation of actual arguments to action methods.
     */
    final Stack<ActionParameter> actionParameters = new Stack<ActionParameter>();

    /**
     * The immutable reference to your parser actions.
     */
    public final A actions;

    /**
     * Constructs a new parser instance without parser actions.
     */
    protected BaseParser() {
        this(null);
    }

    /**
     * Constructs a new parser instance using the given actions instance. Note that if the actions instance is not null
     * it must have been created with {@link Parboiled#createActions(Class, Object[])} )}.
     *
     * @param actions the parser actions (can be null)
     */
    @SuppressWarnings({"unchecked"})
    protected BaseParser(A actions) {
        this.actions = actions;

        List<Class<?>> typeArguments = Utils.getTypeArguments(BaseParser.class, getClass());
        Preconditions.checkState(typeArguments.size() == 2);
        nodeValueType = (Class<V>) typeArguments.get(0);

        if (actions != null) {
            verifyActionsObject();
        }
    }

    private void verifyActionsObject() {
        if (actions instanceof Factory) {
            Callback actionsCallback = ((Factory) actions).getCallback(1);
            if (actionsCallback instanceof ActionInterceptor) {
                ActionInterceptor actionInterceptor = (ActionInterceptor) actionsCallback;
                // signal to the ActionInterceptor that we are in the rule construction phase
                // by informing it about the parser object instance
                actionInterceptor.setParser(this);
                return;
            }
        }
        Checks.fail("Illegal Actions instance, please use Parboiled.createActions(...) " +
                "for creating your parser actions object");
    }

    /**
     * Runs the given parser rule against the given input string. Note that the rule must have been created by
     * a rule creation method of this parser object, which must have been created with
     * {@link Parboiled#createParser(Class, Object[])}.
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input) {
        Checks.ensure(this instanceof Factory && ((Factory) this).getCallback(1) instanceof RuleInterceptor,
                "Illegal parser instance, please use Parboiled.createParser(...) for creating this parser");

        // prepare
        InputBuffer inputBuffer = new InputBuffer(input);
        InputLocation startLocation = new InputLocation(inputBuffer);
        List<ParseError> parseErrors = new ArrayList<ParseError>();
        Matcher<V> matcher = (Matcher<V>) toRule(rule);
        MatcherContext<V> context = new MatcherContext<V>(inputBuffer, startLocation, matcher, parseErrors);

        // the matcher tree has already been built, usually immediately before the invocation of this method,
        // we need to signal to the ActionInterceptor that rule construction is over and all further action
        // calls should not continue to create ActionCallParameters but actually be "routed through" to the
        // actual action method implementations
        if (actions != null) {
            ActionInterceptor actionInterceptor = (ActionInterceptor) ((Factory) actions).getCallback(1);
            actionInterceptor.setParser(null);
        }

        // run the actual matcher tree
        context.runMatcher(true);

        return new ParsingResult<V>(context.getNode(), parseErrors, inputBuffer);
    }

    ////////////////////////////////// RULE CREATION ///////////////////////////////////

    /**
     * Explicitly creates a rule matching the given character. Normally you can just specify the character literal
     * directly in you rule description. However, if you want to not go through {@link #fromCharLiteral(char)},
     * e.g. because you redefined it, you can also use this wrapper.
     *
     * @param c the char to match
     * @return a new rule
     */
    public Rule ch(char c) {
        return new CharMatcher(c);
    }

    /**
     * Explicitly creates a rule matching the given character ignoring the case.
     *
     * @param c the char to match independently of its case
     * @return a new rule
     */
    public Rule charIgnoreCase(char c) {
        return Character.isLetter(c) ? new CharIgnoreCaseMatcher(c) : ch(c);
    }

    /**
     * Creates a rule matching a range of characters from cLow to cHigh (both inclusively).
     *
     * @param cLow  the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a new rule
     */
    public Rule charRange(char cLow, char cHigh) {
        return cLow == cHigh ? ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule string(@NotNull String string) {
        if (string.length() == 1) return ch(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            Rule rule = cached(c);
            matchers[i] = rule != null ? rule : cache(c, ch(c));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule stringIgnoreCase(String string) {
        if (string.length() == 1) return charIgnoreCase(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            boolean letter = Character.isLetter(c);
            Object key = letter ? new IgnoreCaseWrapper(c) : c;
            Rule rule = cached(key);
            matchers[i] = rule != null ? rule : cache(key, letter ? charIgnoreCase(c) : ch(c));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule firstOf(Object rule, Object rule2, Object... moreRules) {
        return new FirstOfMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules)))).label("firstOf");
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule oneOrMore(Object rule) {
        return new OneOrMoreMatcher(toRule(rule)).label("oneOrMore");
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its subrule.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule optional(Object rule) {
        return new OptionalMatcher(toRule(rule)).label("optional");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule sequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), false).label("sequence");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrules succeed, one after the other.
     * However, after the first subrule has matched all further subrule matches are enforced, i.e. if one of them
     * fails a ParseError will be created (and error recovery will be tried).
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule enforcedSequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), true).label("enforcedSequence");
    }

    /**
     * Creates a new rule that acts as a syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule succeeds and fails if the
     * subrule rails. Since this rule does not actually consume any input it will never create a parse tree node.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule test(Object rule) {
        return new TestMatcher(toRule(rule), false);
    }

    /**
     * Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule fails and fails if the
     * subrule succeeds. Since this rule does not actually consume any input it will never create a parse tree node.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule testNot(Object rule) {
        return new TestMatcher(toRule(rule), true);
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule zeroOrMore(Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule)).label("zeroOrMore");
    }

    /**
     * Matches the EOI (end of input) character.
     *
     * @return a new rule
     */
    public Rule eoi() {
        return ch(Chars.EOI);
    }

    /**
     * Matches any character except {@link org.parboiled.support.Chars#EOI}.
     *
     * @return a new rule
     */
    public Rule any() {
        return ch(Chars.ANY);
    }

    /**
     * Matches nothing and therefore always succeeds.
     *
     * @return a new rule
     */
    public Rule empty() {
        return ch(Chars.EMPTY);
    }

    ////////////////////////////////// ACTION PARAMETERS ///////////////////////////////////

    /**
     * Changes the context scope of all arguments to the current parent scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    public <T> T UP(T argument) {
        actionParameters.add(new UpParameter(mixInParameter(actionParameters, argument)));
        return null;
    }

    /**
     * Changes the context scope of all arguments to the current sub scope. This will only work if this call is
     * at some level wrapped with one or more {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    public <T> T DOWN(T argument) {
        actionParameters.add(new DownParameter(mixInParameter(actionParameters, argument)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the parse tree node found under the given prefix path.
     * See {@link ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link #UP(Object)} or {@link #DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Node<V> NODE(String path) {
        actionParameters.add(new PathNodeParameter(mixInParameter(actionParameters, path)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to a list of all parse tree nodes found under the given prefix path.
     * See {@link ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link #UP(Object)} or {@link #DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Node<V>> NODES(String path) {
        actionParameters.add(new PathNodesParameter(mixInParameter(actionParameters, path)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the last node created during this parsing run. This last node
     * is independent of the current context scope, i.e. {@link #UP(Object)} or {@link #DOWN(Object)} have no influence
     * on it.
     *
     * @return the action parameter
     */
    public Node<V> LAST_NODE() {
        actionParameters.add(new LastNodeParameter());
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the tree value of the current context scope level, i.e.,
     * if there is an explicitly set value it is returned. Otherwise the last non-null child value, or, if there
     * is no such value, null.
     *
     * @return the action parameter
     */
    public V VALUE() {
        actionParameters.add(new TreeValueParameter<V>(nodeValueType));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the value of the given node.
     *
     * @param node the node the get the value from
     * @return the action parameter
     */
    public V VALUE(Node<V> node) {
        actionParameters.add(new ValueParameter<V>(nodeValueType, mixInParameter(actionParameters, node)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the value of the node found under the given prefix path.
     * Equivalent to <code>VALUE(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public V VALUE(String path) {
        return VALUE(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the values of all given nodes.
     *
     * @param nodes the nodes to get the values from
     * @return the action parameter
     */
    public List<V> VALUES(List<Node<V>> nodes) {
        actionParameters.add(new ValuesParameter<V>(mixInParameter(actionParameters, nodes)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to a list of the values of all nodes found under the given prefix path.
     * Equivalent to <code>VALUES(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<V> VALUES(String path) {
        return VALUES(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the value of the last node created during this parsing run.
     * Equivalent to <code>VALUE(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public V LAST_VALUE() {
        return VALUE(LAST_NODE());
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the given parse tree node.
     *
     * @param node the parse tree node
     * @return the action parameter
     */
    public String TEXT(Node<V> node) {
        actionParameters.add(new TextParameter<V>(mixInParameter(actionParameters, node)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the node found under the given prefix path.
     * Equivalent to <code>TEXT(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public String TEXT(String path) {
        return TEXT(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the action parameter
     */
    public List<String> TEXTS(List<Node<V>> nodes) {
        actionParameters.add(new TextsParameter<V>(mixInParameter(actionParameters, nodes)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to a list of the input texts matched by of all nodes found
     * under the given prefix path.
     * Equivalent to <code>TEXTS(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<String> TEXTS(String path) {
        return TEXTS(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the last node created during this parsing run.
     * Equivalent to <code>TEXT(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public String LAST_TEXT() {
        return TEXT(LAST_NODE());
    }

    /**
     * Creates an action parameter that evaluates to the first character of the input text matched by the given parse tree node.
     *
     * @param node the parse tree node
     * @return the action parameter
     */
    public Character CHAR(Node<V> node) {
        actionParameters.add(new CharParameter<V>(mixInParameter(actionParameters, node)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first character of the input text matched by the node found under the given prefix path.
     * Equivalent to <code>CHAR(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Character CHAR(String path) {
        return CHAR(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the first characters of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the action parameter
     */
    public List<Character> CHARS(List<Node<V>> nodes) {
        actionParameters.add(new CharsParameter<V>(mixInParameter(actionParameters, nodes)));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to a list of the first characters of the input texts matched by of all nodes found
     * under the given prefix path.
     * Equivalent to <code>CHARS(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Character> CHARS(String path) {
        return CHARS(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the last node created during this parsing run.
     * Equivalent to <code>CHAR(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public Character LAST_CHAR() {
        return CHAR(LAST_NODE());
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current context
     * scope to the value of the last node created during the current parsing run.
     * Equivalent to <code>SET(LAST_VALUE())</code>.
     *
     * @return a new rule
     */
    public ActionResult SET() {
        return SET(LAST_VALUE());
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current context
     * scope to the given value.
     *
     * @param value the value to set
     * @return a new rule
     */
    public ActionResult SET(V value) {
        actionParameters.add(new SetValueParameter<V>(mixInParameter(actionParameters, value), nodeValueType));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to null. You cannot use <b>null</b> directly in an action call
     * expression. Use this method instead.
     *
     * @return the action parameter
     */
    public Object NULL() {
        actionParameters.add(new NullParameter());
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an object using the given converter.
     *
     * @param text      the text (parameter) to convert
     * @param converter the converter to use
     * @return the action parameter
     */
    public <T> T CONVERT(String text, Converter<T> converter) {
        Object converterArg = mixInParameter(actionParameters, converter);
        Object textArg = mixInParameter(actionParameters, text);
        List<Class<?>> convertedTypes = Utils.getTypeArguments(Converter.class, converterArg.getClass());
        Preconditions.checkArgument(convertedTypes.size() == 1, "Illegal converter");
        actionParameters.add(new ConvertParameter(convertedTypes.get(0), textArg, converterArg));
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an Integer.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Integer CONVERT_TO_INTEGER(String text) {
        return CONVERT(text, new Converter<Integer>() {
            public Integer parse(String string) {
                return Integer.parseInt(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Long.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Long CONVERT_TO_LONG(String text) {
        return CONVERT(text, new Converter<Long>() {
            public Long parse(String string) {
                return Long.parseLong(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Float.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Float CONVERT_TO_FLOAT(String text) {
        return CONVERT(text, new Converter<Float>() {
            public Float parse(String string) {
                return Float.parseFloat(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Double.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Double CONVERT_TO_DOUBLE(String text) {
        return CONVERT(text, new Converter<Double>() {
            public Double parse(String string) {
                return Double.parseDouble(string);
            }
        });
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
     * Returns the rule cache content for the given key.
     * @param key the cache key
     * @return the cached rule or null
     */
    protected Rule cached(Object key) {
        return ruleCache.get(key);
    }

    /**
     * Caches the given rule in the rule cache under the given key and return the rule.
     * @param key the key to store the rule under in the cache
     * @param rule the rule to cache
     * @return the rule
     */
    protected Rule cache(Object key, Rule rule) {
        ruleCache.put(key, rule);
        return rule;
    }

    private Rule[] toRules(@NotNull Object[] objects) {
        Rule[] rules = new Rule[objects.length];
        for (int i = 0; i < objects.length; i++) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    private Rule toRule(Object obj) {
        obj = mixInParameter(actionParameters, obj);

        if (obj instanceof Rule) return (Rule) obj;

        Rule rule = cached(obj);
        if (rule != null) return rule;
        
        if (obj instanceof Character) return cache(obj, fromCharLiteral((Character) obj));
        if (obj instanceof String) return cache(obj, fromStringLiteral((String) obj));
        if (obj instanceof ActionParameter) return new ActionMatcher((ActionParameter) obj);

        rule = fromUserObject(obj);
        if (rule != null) return cache(obj, rule);

        throw new ParserConstructionException("\'" + obj + "\' is not a valid Rule or parser action");
    }

    /**
     * Attempts to convert the given object into a rule.
     * Override this method in order to be able to use custom objects directly in your rule specification.
     * The method should return null if the given object cannot be converted into a rule (which is all the
     * default implementation does).
     * Note that the results are automatically cached, so the method is never called twice for equal objects.
     * @param obj the object to convert
     * @return the rule for the object or null
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Rule fromUserObject(Object obj) {
        return null;
    }

    /**
     * Wrapper for rule cache keys that are used for explicit "ignore-case matchers"
     */
    protected static class IgnoreCaseWrapper {
        private final Object key;

        public IgnoreCaseWrapper(Object key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof IgnoreCaseWrapper && key.equals(((IgnoreCaseWrapper) o).key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
