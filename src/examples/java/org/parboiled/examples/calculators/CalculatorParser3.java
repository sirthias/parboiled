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
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.common.StringUtils;
import org.parboiled.examples.calculators.CalculatorParser3.CalcNode;
import org.parboiled.support.Var;
import org.parboiled.trees.ImmutableBinaryTreeNode;

/**
 * A calculator parser building an AST representing the expression structure before performing the actual calculation.
 * The value field of the parse tree nodes is used for AST nodes.
 * As opposed to the CalculatorParser2 this parser also supports floating point operations, negative numbers, a "power"
 * and a "SQRT" operation as well as optional whitespace between the various expressions components.
 */
@BuildParseTree
public class CalculatorParser3 extends CalculatorParser<CalcNode> {

    @Override
    public Rule InputLine() {
        return Sequence(Expression(), Eoi());
    }

    public Rule Expression() {
        Var<Character> op = new Var<Character>();
        return Sequence(
                Term(),
                ZeroOrMore(
                        Sequence(
                                // we use a FirstOf(...) instead of a CharSet so we can use the FromStringLiteral
                                // transformation (see below), which automatically consumes trailing whitespace
                                FirstOf("+ ", "- "), op.set(matchedChar()),
                                Term(),

                                // same as in CalculatorParser2
                                push(new CalcNode(op.get(), pop(1), pop()))
                        )
                )
        );
    }

    public Rule Term() {
        Var<Character> op = new Var<Character>();
        return Sequence(
                Factor(),
                ZeroOrMore(
                        Sequence(
                                FirstOf("* ", "/ "), op.set(matchedChar()),
                                Factor(),
                                push(new CalcNode(op.get(), pop(1), pop()))
                        )
                )
        );
    }

    public Rule Factor() {
        return Sequence(
                Atom(),
                ZeroOrMore(
                        Sequence(
                                "^ ",
                                Atom(),
                                push(new CalcNode('^', pop(1), pop()))
                        )
                )
        );
    }

    public Rule Atom() {
        return FirstOf(Number(), SquareRoot(), Parens());
    }

    public Rule SquareRoot() {
        return Sequence(
                "SQRT ",
                Parens(),

                // create a new AST node with a special operator 'R' and only one child
                push(new CalcNode('R', pop(), null))
        );
    }

    public Rule Parens() {
        return Sequence("( ", Expression(), ") ");
    }

    public Rule Number() {
        return Sequence(
                // we use another Sequence in the "Number" Sequence so we can easily access the input text matched
                // by the three enclosed rules with "lastText()"
                Sequence(
                        Optional('-'),
                        OneOrMore(Digit()),
                        Optional(Sequence('.', OneOrMore(Digit())))
                ),

                // the match() call returns the matched input text of the immediately preceding rule
                push(new CalcNode(Double.parseDouble(match()))),
                WhiteSpace()
        );
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    public Rule WhiteSpace() {
        return ZeroOrMore(CharSet(" \t\f"));
    }

    // we redefine the rule creation for string literals to automatically match trailing whitespace if the string
    // literal ends with a space character, this way we don't have to insert extra whitespace() rules after each
    // character or string literal

    @Override
    protected Rule FromStringLiteral(@NotNull String string) {
        return string.endsWith(" ") ?
                Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace()) :
                String(string);
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
        main(CalculatorParser3.class);
    }
}