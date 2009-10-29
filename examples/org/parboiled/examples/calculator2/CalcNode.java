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

package org.parboiled.examples.calculator2;

import org.parboiled.ast.MutableLeftRightAstNodeImpl;

/**
 * The AST node implementation for the calculator.
 * The type of the node is carried as a Character that can either contain an operator or be null. In this case
 * the AST node is a leaf.
 */
public class CalcNode extends MutableLeftRightAstNodeImpl<Character, CalcNode> {

    private int value;

    public CalcNode(int value) {
        this.value = value;
    }

    public CalcNode(Character type, CalcNode left, CalcNode right) {
        setType(type);
        setLeft(left);
        setRight(right);
    }

    public boolean isOneOf(String operations) {
        return getType() != null && operations.indexOf(getType()) >= 0;
    }

    public int getValue() {
        if (getType() == null) return value;

        switch (getType()) {
            case '+':
                return left().getValue() + right().getValue();
            case '-':
                return left().getValue() - right().getValue();
            case '*':
                return left().getValue() * right().getValue();
            case '/':
                return left().getValue() / right().getValue();
            default:
                throw new IllegalStateException();
        }
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getType() == null ?
                "Value " + value :
                "Operator '" + getType() + '\'';
    }
    
}
