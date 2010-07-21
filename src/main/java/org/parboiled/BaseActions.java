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
import org.parboiled.common.StringUtils;
import org.parboiled.support.Checks;

/**
 * Convenience context aware base class defining a number of useful helper methods.
 *
 * @param <V> the type of the parser values
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class BaseActions<V> implements ContextAware {

    private Context context;

    /**
     * The current context for use with action methods. Updated immediately before action calls.
     *
     * @return the current context
     */
    public Context getContext() {
        return context;
    }

    /**
     * ContextAware interface implementation.
     *
     * @param context the context
     */
    public void setContext(@NotNull Context context) {
        this.context = context;
    }

    /**
     * <p>Returns the input text matched by the context immediately preceeding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the input text matched by the immediately preceeding subrule
     */
    public String match() {
        check();
        return context.getMatch();
    }

    public char matchedChar() {
        return match().charAt(0);
    }

    /**
     * <p>Returns the start index of the matched rule immediately preceeding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the start index of the context immediately preceeding current action
     */
    public int matchStart() {
        check();
        return context.getMatchStartIndex();
    }

    /**
     * <p>Returns the end location of the matched rule immediately preceeding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the end index of the context immediately preceeding current action, i.e. the index of the character
     *         immediately following the last matched character
     */
    public int matchEnd() {
        check();
        return context.getMatchEndIndex();
    }

    public boolean push(V value) {
        check();
        context.push(value);
        return true;
    }

    public boolean push(V... values) {
        check();
        context.push(values);
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public V pop() {
        check();
        return (V) context.pop();
    }

    @SuppressWarnings({"unchecked"})
    public V peek() {
        check();
        return (V) context.peek();
    }

    public boolean poke(V value) {
        check();
        context.poke(value);
        return true;
    }

    public boolean dup() {
        check();
        context.push(context.peek());
        return true;
    }

    public boolean swap() {
        check();
        context.swap();
        return true;
    }

    public boolean swap3() {
        check();
        context.swap3();
        return true;
    }

    public boolean swap4() {
        check();
        context.swap4();
        return true;
    }

    public boolean swap5() {
        check();
        context.swap5();
        return true;
    }

    public boolean swap6() {
        check();
        context.swap6();
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
