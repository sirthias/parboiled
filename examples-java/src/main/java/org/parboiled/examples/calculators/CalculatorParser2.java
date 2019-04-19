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

package org.parboiled.examples.calculators;

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.examples.calculators.CalculatorParser2.CalcNode;
import org.parboiled.support.Var;
import org.parboiled.trees.ImmutableBinaryTreeNode;

/**
 * A calculator parser building an AST representing the expression structure before performing the actual calculation.
 * The parser value stack is used to build the AST nodes of type CalcNode.
 */
@BuildParseTree
public class CalculatorParser2 extends CalculatorParser<CalcNode> {

    @Override
    public Rule InputLine() {
        return Sequence(Expression(), EOI);
    }

    public Rule Expression() {
        Var<Character> op = new Var<Character>(); // we use an action variable to hold the operator character
        return Sequence(
                Term(),
                ZeroOrMore(
                        AnyOf("+-"),
                        op.set(matchedChar()), // set the action variable to the matched operator char
                        Term(),

                        // create an AST node for the operation that was just matched
                        // we consume the two top stack elements and replace them with a new AST node
                        // we use an alternative technique to the one shown in CalculatorParser1 to reverse
                        // the order of the two top value stack elements
                        swap() && push(new CalcNode(op.get(), pop(), pop()))
                )
        );
    }

    public Rule Term() {
        Var<Character> op = new Var<Character>(); // we use an action variable to hold the operator character
        return Sequence(
                Factor(),
                ZeroOrMore(
                        AnyOf("*/"),
                        op.set(matchedChar()), // set the action variable to the matched operator char
                        Factor(),

                        // create an AST node for the operation that was just matched
                        // we consume the two top stack elements and replace them with a new AST node
                        // we use an alternative technique to the one shown in CalculatorParser1 to reverse
                        // the order of the two top value stack elements
                        swap() && push(new CalcNode(op.get(), pop(), pop()))
                )
        );
    }

    public Rule Factor() {
        return FirstOf(Number(), Parens());
    }

    public Rule Parens() {
        return Sequence('(', Expression(), ')');
    }

    public Rule Number() {
        return Sequence(
                Digits(),

                // parse the input text matched by the preceding "Digits" rule,
                // convert it into an Integer and push a new AST node for it onto the value stack
                // the action uses a default string in case it is run during error recovery (resynchronization)
                push(new CalcNode(Integer.parseInt(matchOrDefault("0"))))
        );
    }

    @SuppressSubnodes
    public Rule Digits() {
        return OneOrMore(Digit());
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    //****************************************************************

    /**
     * The AST node for the calculators. The type of the node is carried as a Character that can either contain
     * an operator char or be null. In the latter case the AST node is a leaf directly containing a value.
     */
    public static class CalcNode extends ImmutableBinaryTreeNode<CalcNode> {
        private int value;
        private Character operator;

        public CalcNode(int value) {
            super(null, null);
            this.value = value;
        }

        public CalcNode(Character operator, CalcNode left, CalcNode right) {
            super(left, right);
            this.operator = operator;
        }

        public int getValue() {
            if (operator == null) return value;
            switch (operator) {
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

        @Override
        public String toString() {
            return (operator == null ? "Value " + value : "Operator '" + operator + '\'') + " | " + getValue();
        }

    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser2.class);
    }

}
