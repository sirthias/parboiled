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

package org.parboiled.common;

public class IntArrayStack {

    public static class UnderflowException extends RuntimeException {
        public UnderflowException(String message) {
            super(message);
        }
    }

    private static final int INITIAL_CAPACITY = 16;
    private int[] array;
    private int top;

    public IntArrayStack() {
        array = new int[INITIAL_CAPACITY];
        top = -1;
    }

    /**
     * Tests if the stack is empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * Returns the number of element currently on the stack.
     *
     * @return the number of element currently on the stack
     */
    public int size() {
        return top + 1;
    }

    /**
     * Copies all elements currently on the stack into the given array.
     *
     * @param destArray the array
     * @param destStartIndex the index to start copying into
     */
    public void getElements(int[] destArray, int destStartIndex) {
        System.arraycopy(array, 0, destArray, destStartIndex, size());
    }

    /**
     * @return all elements in a new array.
     */
    public int[] toArray() {
        int[] array = new int[size()];
        getElements(array, 0);
        return array;
    }

    /**
     * Empties the stack.
     */
    public void clear() {
        top = -1;
    }

    /**
     * Returns the item at the top of the stack without removing it.
     *
     * @return the most recently inserted item in the stack.
     * @throws UnderflowException if the stack is empty.
     */
    public int peek() {
        if (isEmpty()) {
            throw new UnderflowException("IntArrayStack peek");
        }
        return array[top];
    }

    /**
     * Removes the most recently inserted item from the stack.
     *
     * @return the top stack item
     * @throws UnderflowException if the stack is empty.
     */
    public int pop() {
        if (isEmpty()) {
            throw new UnderflowException("IntArrayStack pop");
        }
        return array[top--];
    }

    /**
     * Pushes a new item onto the stack.
     *
     * @param x the item to add.
     */
    public void push(int x) {
        if (top == array.length - 1) {
            expandCapacity();
        }
        array[++top] = x;
    }

    private void expandCapacity() {
        int[] newArray = new int[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
    }
}
