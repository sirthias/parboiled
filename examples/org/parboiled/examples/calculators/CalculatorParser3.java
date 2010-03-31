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
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return sequence(
                term(), SET(), // the SET() sets the value of the "expression" to the value of the preceding "term"
                zeroOrMore(
                        sequence(
                                charSet("+-").label("op"),
                                term(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // for the "expression" sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "expression")
                                // - the old value of the "expression" as left child
                                // - the value of the preceding "term" as right child
                                UP2(SET(createAst(DOWN2(CHAR("op")), VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule term() {
        return sequence(
                factor(), SET(), // the SET() sets the value of the "expression" to the value of the preceding "factor"
                zeroOrMore(
                        sequence(
                                charSet("*/").label("op"),
                                factor(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // one for the "term" sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "term")
                                // - the old value of the "term" as left child
                                // - the value of the preceding "factor" as right child
                                UP2(SET(createAst(DOWN2(CHAR("op")), VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule factor() {
        return firstOf(number(), parens());
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(
                digits(),

                // parse the input text matched by the preceding "digits" rule, convert it into an Integer and set this
                // Integer as the value of the parse tree node of this rule (the sequence rule labelled "number")
                SET(createAst(Integer.parseInt(LAST_TEXT())))
        );
    }

    @Leaf
    public Rule digits() {
        return oneOrMore(digit());
    }

    public Rule digit() {
        return charRange('0', '9');
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
            return (operator == null ? "Value " + value : "Operator '" + operator + '\'') + '|' + getValue();
        }

    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser3.class);
    }

}