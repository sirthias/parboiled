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
    public Rule InputLine() {
        return Sequence(Expression(), Eoi());
    }

    public Rule Expression() {
        return Sequence(
                Term(), set(), // the set() sets the value of the "Expression" to the value of the preceding "Term"
                ZeroOrMore(
                        Sequence(
                                // we use a FirstOf(...) instead of a CharSet so we can use the FromCharLiteral transformation
                                FirstOf('+', '-').label("op"),
                                Term(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // one for the "Expression" Sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "Expression")
                                // - the old value of the "Expression" as left child
                                // - the value of the preceding "Term" as right child
                                UP2(set(createAst(DOWN2(character("op")), value(), lastValue())))
                        )
                )
        );
    }

    public Rule Term() {
        return Sequence(
                Factor(), set(), // the set() sets the value of the "Term" to the value of the preceding "Factor"
                ZeroOrMore(
                        Sequence(
                                // we use a FirstOf(...) instead of a CharSet so we can use the FromCharLiteral transformation
                                FirstOf('*', '/').label("op"),
                                Factor(),

                                // create an AST node for the operation that was just matched
                                // The new AST node is not set on the parse tree node created for this rule, but on the
                                // one for the "Term" Sequence two levels up. The arguments for the AST node are
                                // - the operator that matched (which is two levels underneath the "Term")
                                // - the old value of the "Term" as left child
                                // - the value of the preceding "Factor" as right child
                                UP2(set(createAst(DOWN2(character("op")), value(), lastValue())))
                        )
                )
        );
    }

    public Rule Factor() {
        return Sequence(
                Atom(), set(), // the set() sets the value of the "Factor" to the value of the preceding "Atom"
                ZeroOrMore(
                        Sequence(
                                '^',
                                Atom(),

                                // create a new AST node and set it as the value of the parse tree node for the
                                // "Factor" rule, the node contains the operator ('^'), the old
                                // "Factor" value as left child and the value of the "Atom" following
                                // the operator as right child
                                UP2(set(createAst('^', value(), lastValue())))
                        )
                )
        );
    }

    public Rule Atom() {
        return FirstOf(Number(), SquareRoot(), Parens());
    }

    public Rule SquareRoot() {
        return Sequence(
                "SQRT",
                Parens(),

                // create a new AST node with a special operator 'R' and only one child
                set(createAst('R', lastValue(), null))
        );
    }

    public Rule Parens() {
        return Sequence('(', Expression(), ')');
    }

    public Rule Number() {
        return Sequence(
                // we use another Sequence in the "Number" Sequence so we can easily access the input text matched
                // by the three enclosed rules with "lastText()"
                Sequence(
                        Optional(Ch('-')),
                        OneOrMore(Digit()),
                        Optional(Sequence(Ch('.'), OneOrMore(Digit())))
                ),
                set(createAst(Double.parseDouble(lastText()))),
                WhiteSpace()
        );
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    public Rule WhiteSpace() {
        return ZeroOrMore(CharSet(" \t\f"));
    }

    // we redefine the rule creation for character literals to also match trailing whitespace this way we don't have
    // to insert extra whitespace() rules after each character literal however, we now have to wrap character matching
    // rules we don't want to be "space swallowing" with the Ch(...) rule creator

    @Override
    protected Rule FromCharLiteral(char c) {
        return Sequence(Ch(c), WhiteSpace());
    }

    // same thing for String literals

    @Override
    protected Rule FromStringLiteral(@NotNull String string) {
        return Sequence(String(string), WhiteSpace());
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
            return (operator == null ? "Value " + value : "Operator '" + operator + '\'') + " | " + getValue();
        }
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser4.class);
    }
}