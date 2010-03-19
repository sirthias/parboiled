/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.transform;

import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.support.Checks;

/**
 * Base class of generated action classes wrapping action expressions.
 *
 * @param <V> the type of the value field of a parse tree node
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class ActionWrapperBase<V> implements Action<V> {

    protected Context<V> context;

    protected final void UP() {
        Context<V> parentContext = context.getParent();
        Checks.ensure(parentContext != null, "Illegal UP() call, already at root level");
        this.context = parentContext;
    }
    
    protected final void UP2() {
        UP();
        UP();
    }
    
    protected final void UP3() {
        UP();
        UP();
        UP();
    }
    
    protected final void UP4() {
        UP();
        UP();
        UP();
        UP();
    }

    protected final void UP5() {
        UP();
        UP();
        UP();
        UP();
        UP();
    }

    protected final void UP6() {
        UP();
        UP();
        UP();
        UP();
        UP();
        UP();
    }

    protected final void DOWN() {
        Context<V> subContext = context.getSubContext();
        Checks.ensure(subContext != null, "Illegal DOWN() call, already at leaf level");
        this.context = subContext;
    }
    
    protected final void DOWN2() {
        DOWN();
        DOWN();
    }
    
    protected final void DOWN3() {
        DOWN();
        DOWN();
        DOWN();
    }
    
    protected final void DOWN4() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    protected final void DOWN5() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    protected final void DOWN6() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
