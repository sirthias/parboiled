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

package org.parboiled.examples.calculators;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Rule;
import org.parboiled.examples.calculators.CalculatorParser3.CalcNode;
import org.parboiled.support.Leaf;
import org.parboiled.trees.ImmutableBinaryTreeNode;

/**
 * A calculator parser building an AST representing the expression structure before performing the actual calculation.
 * The value field of the parse tree nodes is used for AST nodes.
 */
public class CalculatorParser3 extends CalculatorParser<CalcNode> {

    @Override
    public Rule InputLine() {
        return Sequence(Expression(), Eoi());
    }

    public Rule Expression() {
        return Sequence(
                Term(), set(), // the set() sets the value of the "Expression" to the value of the preceding "Term"
                ZeroOrMore(
                        Sequence(
                                CharSet("+-").label("Op"),
                                Term(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // for the "Expression" Sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "Expression")
                                // - the old value of the "Expression" as left child
                                // - the value of the preceding "Term" as right child
                                UP2(set(createAst(DOWN2(character("Op")), value(), lastValue())))
                        )
                )
        );
    }

    public Rule Term() {
        return Sequence(
                Factor(), set(), // the set() sets the value of the "Expression" to the value of the preceding "Factor"
                ZeroOrMore(
                        Sequence(
                                CharSet("*/").label("Op"),
                                Factor(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // one for the "Term" Sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "Term")
                                // - the old value of the "Term" as left child
                                // - the value of the preceding "Factor" as right child
                                UP2(set(createAst(DOWN2(character("Op")), value(), lastValue())))
                        )
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

                // parse the input text matched by the preceding "Digits" rule, convert it into an Integer and set this
                // Integer as the value of the parse tree node of this rule (the Sequence rule labelled "Number")
                set(createAst(Integer.parseInt(lastText())))
        );
    }

    @Leaf
    public Rule Digits() {
        return OneOrMore(Digit());
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    //**************** ACTIONS ****************

    public CalcNode createAst(Integer value) {
        return new CalcNode(value);
    }

    public CalcNode createAst(Character type, CalcNode left, CalcNode right) {
        return new CalcNode(type, left, right);
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

        public CalcNode(@NotNull Character operator, @NotNull CalcNode left, @NotNull CalcNode right) {
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
        main(CalculatorParser3.class);
    }

}