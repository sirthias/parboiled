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
import org.parboiled.examples.calculators.CalculatorParser4.CalcNode;
import org.parboiled.trees.ImmutableBinaryTreeNode;

/**
 * A calculator parser building an AST representing the expression structure before performing the actual calculation.
 * The value field of the parse tree nodes is used for AST nodes.
 * As opposed to the CalculatorParser3 this parser also supports floating point operations and negative numbers as
 * well as a "power" and a "SQRT" operation.
 */
public class CalculatorParser4 extends CalculatorParser<CalcNode> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return sequence(
                term(), SET(), // the SET() sets the value of the "expression" to the value of the preceding "term"
                zeroOrMore(
                        sequence(
                                // we use a firstOf(...) instead of a charSet so we can use the fromCharLiteral transformation
                                firstOf('+', '-').label("op"),
                                term(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // one for the "expression" sequence two levels up. The arguments for the AST node are
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
                factor(), SET(), // the SET() sets the value of the "term" to the value of the preceding "factor"
                zeroOrMore(
                        sequence(
                                // we use a firstOf(...) instead of a charSet so we can use the fromCharLiteral transformation
                                firstOf('*', '/').label("op"),
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
        return sequence(
                atom(), SET(), // the SET() sets the value of the "factor" to the value of the preceding "atom"
                zeroOrMore(
                        sequence(
                                '^',
                                atom(),

                                // create a new AST node and set it as the value of the parse tree node for the
                                // "factor" rule, the node contains the operator ('^'), the old
                                // "factor" value as left child and the value of the "atom" following
                                // the operator as right child
                                UP2(SET(createAst('^', VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule atom() {
        return firstOf(number(), squareRoot(), parens());
    }

    public Rule squareRoot() {
        return sequence(
                "SQRT",
                parens(),

                // create a new AST node with a special operator 'R' and only one child
                SET(createAst('R', LAST_VALUE(), null))
        );
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(
                // we use another sequence in the "number" sequence so we can easily access the input text matched
                // by the three enclosed rules with "LAST_TEXT()"
                sequence(
                        optional(ch('-')),
                        oneOrMore(digit()),
                        optional(sequence(ch('.'), oneOrMore(digit())))
                ),
                SET(createAst(Double.parseDouble(LAST_TEXT()))),
                whiteSpace()
        );
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    public Rule whiteSpace() {
        return zeroOrMore(charSet(" \t\f"));
    }

    // we redefine the rule creation for character literals to also match trailing whitespace this way we don't have
    // to insert extra whitespace() rules after each character literal however, we now have to wrap character matching
    // rules we don't want to be "space swallowing" with the ch(...) rule creator

    @Override
    protected Rule fromCharLiteral(char c) {
        return sequence(ch(c), whiteSpace());
    }

    // same thing for string literals

    @Override
    protected Rule fromStringLiteral(@NotNull String string) {
        return sequence(string(string), whiteSpace());
    }

    //**************** ACTIONS ****************

    public CalcNode createAst(double value) {
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
        private double value;
        private Character operator;

        public CalcNode(double value) {
            super(null, null);
            this.value = value;
        }

        public CalcNode(@NotNull Character operator, @NotNull CalcNode left, CalcNode right) {
            super(left, right);
            this.operator = operator;
        }

        public double getValue() {
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
                case '^':
                    return Math.pow(left().getValue(), right().getValue());
                case 'R':
                    return Math.sqrt(left().getValue());
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
        main(CalculatorParser4.class);
    }
}