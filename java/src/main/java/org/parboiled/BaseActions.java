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

import org.parboiled.support.Checks;
import org.parboiled.support.IndexRange;
import org.parboiled.support.Position;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * Convenience context aware base class defining a number of useful helper methods.
 *
 * @param <V> the type of the parser values
 */
@SuppressWarnings( {"UnusedDeclaration"})
public abstract class BaseActions<V> implements ContextAware<V> {

    private Context<V> context;

    /**
     * The current context for use with action methods. Updated immediately before action calls.
     *
     * @return the current context
     */
    public Context<V> getContext() {
        return context;
    }

    /**
     * ContextAware interface implementation.
     *
     * @param context the context
     */
    public void setContext(Context<V> context) {
        this.context = checkArgNotNull(context, "context");
    }

    /**
     * Returns the current index in the input buffer.
     *
     * @return the current index
     */
    public int currentIndex() {
        check();
        return context.getCurrentIndex();
    }

    /**
     * <p>Returns the input text matched by the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the input text matched by the immediately preceding subrule
     */
    public String match() {
        check();
        return context.getMatch();
    }
    
    /**
     * Creates a new {@link IndexRange} instance covering the input text matched by the rule immediately preceding the
     * action expression that is currently being evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.
     *  
     * @return a new IndexRange instance
     */
    public IndexRange matchRange() {
        check();
        return context.getMatchRange();
    }

    /**
     * <p>Returns the input text matched by the rule immediately preceding the action expression that is currently
     * being evaluated. If the matched input text is empty the given default string is returned.
     * This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @param defaultString the default string to return if the matched input text is empty
     * @return the input text matched by the immediately preceding subrule or the default string
     */
    public String matchOrDefault(String defaultString) {
        check();
        String match = context.getMatch();
        return match.length() == 0 ? defaultString : match;
    }

    /**
     * <p>Returns the first character of the input text matched by the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used in actions that are part of a Sequence
     * rule and are not at first position in this Sequence.</p>
     * <p>If the immediately preceding rule did not match anything this method throws a GrammarException. If you need
     * to able to handle that case use the getMatch() method.</p>
     *
     * @return the first input char of the input text matched by the immediately preceding subrule or null,
     *         if the previous rule matched nothing
     */
    public char matchedChar() {
        check();
        return context.getFirstMatchChar();
    }

    /**
     * <p>Returns the start index of the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the start index of the context immediately preceding current action
     */
    public int matchStart() {
        check();
        return context.getMatchStartIndex();
    }

    /**
     * <p>Returns the end location of the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the end index of the context immediately preceding current action, i.e. the index of the character
     *         immediately following the last matched character
     */
    public int matchEnd() {
        check();
        return context.getMatchEndIndex();
    }
    
    /**
     * <p>Returns the number of characters matched by the rule immediately preceding the action expression that is
     * currently being evaluated. This call can only be used in actions that are part of a Sequence rule and are not
     * at first position in this Sequence.</p>
     * 
     * @return the number of characters matched
     */
    public int matchLength() {
        check();
        return context.getMatchLength();
    }

    /**
     * <p>Returns the current position in the underlying {@link org.parboiled.buffers.InputBuffer} as a
     * {@link Position} instance.</p>
     * 
     * @return the current position in the underlying inputbuffer
     */
    public Position position() {
        check();
        return context.getPosition();
    }

    /**
     * Pushes the given value onto the value stack. Equivalent to push(0, value).
     *
     * @param value the value to push
     * @return true
     */
    public boolean push(V value) {
        check();
        context.getValueStack().push(value);
        return true;
    }

    /**
     * Inserts the given value a given number of elements below the current top of the value stack.
     *
     * @param down  the number of elements to skip before inserting the value (0 being equivalent to push(value))
     * @param value the value
     * @return true
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public boolean push(int down, V value) {
        check();
        context.getValueStack().push(down, value);
        return true;
    }

    /**
     * Pushes all given elements onto the value stack (in the order as given).
     *
     * @param firstValue the first value
     * @param moreValues the other values
     * @return true
     */
    public boolean pushAll(V firstValue, V... moreValues) {
        check();
        context.getValueStack().pushAll(firstValue, moreValues);
        return true;
    }

    /**
     * Removes the value at the top of the value stack and returns it.
     *
     * @return the current top value
     * @throws IllegalArgumentException if the stack is empty
     */
    public V pop() {
        check();
        return context.getValueStack().pop();
    }

    /**
     * Removes the value the given number of elements below the top of the value stack.
     *
     * @param down the number of elements to skip before removing the value (0 being equivalent to pop())
     * @return the value
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public V pop(int down) {
        check();
        return context.getValueStack().pop(down);
    }

    /**
     * Removes the value at the top of the value stack.
     *
     * @return true
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean drop() {
        check();
        context.getValueStack().pop();
        return true;
    }

    /**
     * Removes the value the given number of elements below the top of the value stack.
     *
     * @param down the number of elements to skip before removing the value (0 being equivalent to drop())
     * @return true
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public boolean drop(int down) {
        check();
        context.getValueStack().pop(down);
        return true;
    }

    /**
     * Returns the value at the top of the value stack without removing it.
     *
     * @return the current top value
     * @throws IllegalArgumentException if the stack is empty
     */
    public V peek() {
        check();
        return context.getValueStack().peek();
    }

    /**
     * Returns the value the given number of elements below the top of the value stack without removing it.
     *
     * @param down the number of elements to skip (0 being equivalent to peek())
     * @return the value
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public V peek(int down) {
        check();
        return context.getValueStack().peek(down);
    }

    /**
     * Replaces the current top value of the value stack with the given value. Equivalent to poke(0, value).
     *
     * @param value the value
     * @return true
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean poke(V value) {
        check();
        context.getValueStack().poke(value);
        return true;
    }

    /**
     * Replaces the element the given number of elements below the current top of the value stack.
     *
     * @param down  the number of elements to skip before replacing the value (0 being equivalent to poke(value))
     * @param value the value to replace with
     * @return true
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public boolean poke(int down, V value) {
        check();
        context.getValueStack().poke(down, value);
        return true;
    }

    /**
     * Duplicates the top value of the value stack. Equivalent to push(peek()).
     *
     * @return true
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean dup() {
        check();
        context.getValueStack().dup();
        return true;
    }

    /**
     * Swaps the top two elements of the value stack.
     *
     * @return true
     * @throws org.parboiled.errors.GrammarException
     *          if the stack does not contain at least two elements
     */
    public boolean swap() {
        check();
        context.getValueStack().swap();
        return true;
    }

    /**
     * Reverses the order of the top 3 value stack elements.
     *
     * @return true
     * @throws org.parboiled.errors.GrammarException
     *          if the stack does not contain at least 3 elements
     */
    public boolean swap3() {
        check();
        context.getValueStack().swap3();
        return true;
    }

    /**
     * Reverses the order of the top 4 value stack elements.
     *
     * @return true
     * @throws org.parboiled.errors.GrammarException
     *          if the stack does not contain at least 4 elements
     */
    public boolean swap4() {
        check();
        context.getValueStack().swap4();
        return true;
    }

    /**
     * Reverses the order of the top 5 value stack elements.
     *
     * @return true
     * @throws org.parboiled.errors.GrammarException
     *          if the stack does not contain at least 5 elements
     */
    public boolean swap5() {
        check();
        context.getValueStack().swap5();
        return true;
    }

    /**
     * Reverses the order of the top 6 value stack elements.
     *
     * @return true
     * @throws org.parboiled.errors.GrammarException
     *          if the stack does not contain at least 6 elements
     */
    public boolean swap6() {
        check();
        context.getValueStack().swap6();
        return true;
    }

    /**
     * Returns the next input character about to be matched.
     *
     * @return the next input character about to be matched
     */
    public Character currentChar() {
        check();
        return context.getCurrentChar();
    }

    /**
     * Returns true if the current rule is running somewhere underneath a Test/TestNot rule.
     * Useful for example for making sure actions are not run inside of a predicate evaluation:
     * <code>
     * return Sequence(
     * ...,
     * inPredicate() || actions.doSomething()
     * );
     * </code>
     *
     * @return true if in a predicate
     */
    public boolean inPredicate() {
        check();
        return context.inPredicate();
    }

    /**
     * Returns true if the current context is for or below a rule marked @SuppressNode or below one
     * marked @SuppressSubnodes.
     *
     * @return true or false
     */
    public boolean nodeSuppressed() {
        check();
        return context.isNodeSuppressed();
    }

    /**
     * Determines whether the current rule or a sub rule has recorded a parse error.
     * Useful for example for making sure actions are not run on erroneous input:
     * <code>
     * return Sequence(
     * ...,
     * !hasError() && actions.doSomething()
     * );
     * </code>
     *
     * @return true if either the current rule or a sub rule has recorded a parse error
     */
    public boolean hasError() {
        check();
        return context.hasError();
    }

    private void check() {
        Checks.ensure(context != null && context.getMatcher() != null,
                "Illegal rule definition: Unwrapped action expression!");
    }

}
