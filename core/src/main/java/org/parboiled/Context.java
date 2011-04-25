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

import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.IndexRange;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.Position;
import org.parboiled.support.ValueStack;

import java.util.List;

/**
 * A Context object is available to parser actions methods during their runtime and provides various support functionalities.
 */
public interface Context<V> {

    /**
     * Returns the parent context, i.e. the context for the currently running parent matcher.
     *
     * @return the parent context
     */
    Context<V> getParent();

    /**
     * Returns the InputBuffer the parser is currently running against
     *
     * @return the InputBuffer
     */
    InputBuffer getInputBuffer();

    /**
     * Returns the Matcher of this context or null, if this context is not valid anymore.
     *
     * @return the matcher
     */
    Matcher getMatcher();

    /**
     * Returns the index into the underlying input buffer where the matcher of this context started its match.
     *
     * @return the start index
     */
    int getStartIndex();

    /**
     * Returns the current index in the input buffer.
     *
     * @return the current index
     */
    int getCurrentIndex();

    /**
     * Returns the character at the current index..
     *
     * @return the current character
     */
    char getCurrentChar();

    /**
     * Returns the list of parse errors for the entire parsing run.
     *
     * @return the list of parse errors
     */
    List<ParseError> getParseErrors();

    /**
     * Returns the {@link MatcherPath} to the currently running matcher.
     *
     * @return the path
     */
    MatcherPath getPath();

    /**
     * Returns the current matcher level, with 0 being the root level, 1 being one level below the root and so on.
     *
     * @return the current matcher level
     */
    int getLevel();

    /**
     * <p>Returns true if fast string matching is enabled for this parsing run.</p>
     * <p>Fast string matching "short-circuits" the default practice of treating string rules as simple Sequence of
     * character rules. When fast string matching is enabled strings are matched at once, without relying on inner
     * CharacterMatchers. Even though this can lead to significant increases of parsing performance it does not play
     * well with error reporting and recovery, which relies on character level matches.
     * Therefore the {@link org.parboiled.parserunners.ReportingParseRunner} and {@link org.parboiled.parserunners.RecoveringParseRunner} implementations only enable fast
     * string matching during their basic first parsing run and disable it once the input has proven to contain errors.
     * </p>
     *
     * @return true if fast string matching is enabled during the current parsing run
     */
    boolean fastStringMatching();

    /**
     * Returns the parse tree subnodes already created in the current context scope.
     * Note that the returned list is immutable.
     *
     * @return the parse tree subnodes already created in the current context scope
     */
    List<Node<V>> getSubNodes();

    /**
     * Determines if the current rule is running somewhere underneath a Test/TestNot rule.
     *
     * @return true if the current context has a parent which corresponds to a Test/TestNot rule
     */
    boolean inPredicate();
    
    /**
     * Determines if the action calling this method is run during the resynchronization phase of an error recovery.
     *
     * @return true if the action calling this method is run during the resynchronization phase of an error recovery
     */
    boolean inErrorRecovery();

    /**
     * Determines if the current context is for or below a rule marked @SuppressNode or below one
     * marked @SuppressSubnodes.
     *
     * @return true or false
     */
    boolean isNodeSuppressed();

    /**
     * Determines if this context or any sub node recorded a parse error.
     *
     * @return true if this context or any sub node recorded a parse error
     */
    boolean hasError();

    /**
     * <p>Returns the input text matched by the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the input text matched by the immediately preceding subcontext
     */
    String getMatch();

    /**
     * <p>Returns the first character of the input text matched by the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used in actions that are part of a Sequence
     * rule and are not at first position in this Sequence.</p>
     * <p>If the immediately preceding rule did not match anything this method throws a GrammarException. If you need
     * to able to handle that case use the getMatch() method.</p>
     *
     * @return the input text matched by the immediately preceding subcontext
     */
    char getFirstMatchChar();

    /**
     * <p>Returns the start index of the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the start index of the context immediately preceding current action
     */
    int getMatchStartIndex();

    /**
     * <p>Returns the end index of the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the end index of the context immediately preceding current action, i.e. the index of the character
     *         immediately following the last matched character
     */
    int getMatchEndIndex();

    /**
     * <p>Returns the number of characters matched by the rule immediately preceding the action expression that is
     * currently being evaluated. This call can only be used in actions that are part of a Sequence rule and are not
     * at first position in this Sequence.</p>
     * 
     * @return the number of characters matched
     */
    int getMatchLength();
    
    /**
     * <p>Returns the current position in the underlying {@link org.parboiled.buffers.InputBuffer} as a
     * {@link Position} instance.</p>
     * 
     * @return the current position in the underlying inputbuffer
     */
    Position getPosition();
    
    /**
     * Creates a new {@link IndexRange} instance covering the input text matched by the rule immediately preceding the
     * action expression that is currently being evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.
     *  
     * @return a new IndexRange instance
     */
    IndexRange getMatchRange();
    
    /**
     * Returns the value stack instance used during this parsing run.
     *
     * @return the value stack
     */
    ValueStack<V> getValueStack();
}

