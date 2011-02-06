/*
 * Copyright (C) 2011 Ken Wenzel
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

package org.parboiled.support;

/**
 * A CallStack is a stack implementation for parser action variables.
 */
public interface CallStack {
    /**
     * Returns the current state of the arguments array.
     * 
     * @return arguments array
     */
    Object[] getArguments();
    
    /**
     * Returns the current state of the locals array.
     * 
     * @return locals array
     */
    Object[] getLocals();
    
    /**
     * Returns the value for the action variable with index <code>i</code>.
     * 
     * @param i index of the action variable
     * @return value of the action variable
     */
    Object getVariable(int i);

    /**
     * Removes the last frame from this call stack.
     */
    void popFrame();
    
    /**
     * Creates a new stack frame with reserved space for <code>numLocals</code> local variables.
     * 
     * @param numLocals number of local variables
     */
    void pushFrame(int numLocals);
    
    /**
     * Sets the action arguments for the next rule invocation. 
     * 
     * @param args arguments for the next rule invocation
     */
    void setArguments(Object[] args);

    /**
     * Sets the action variable with index <code>i</code> to the given <code>value</code>.
     * 
     * @param i index of the action variable
     * @param value value of the action variable
     */
    void setVariable(int i, Object value);
}
