/*
 * Copyright (C) 2010 Reinier Zwitserloot, adapted by Mathias Doenitz
 */

package org.parboiled.examples.rpn;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Leaf;

public class RpnParser extends BaseParser<Node> {

    public final RpnActions actions = new RpnActions();

    public Rule Operation() {
        return Sequence(
                Separator(),
                ZeroOrMore(Atom()).label("sequenceOfAtoms"),
                Eoi(),
                set(actions.runStack(values("s/a"))));
    }

    public Rule Atom() {
        return FirstOf(Number(), BinarySymbol());
    }

    public Rule BinarySymbol() {
        return Sequence(
                FirstOf('+', '-', '*', '/', '^'),
                set(actions.toOperator(lastChar())),
                Separator()
        );
    }

    public Rule Number() {
        return Sequence(
                Sequence(
                        Optional(Minus()),
                        FirstOf(
                                DotNumber(),
                                Sequence(Digits(), Optional(DotNumber()))
                        ),
                        Optional(Exponent())
                ),
                set(actions.toBigDecimal(lastText())),
                Separator()
        );
    }

    @Leaf
    public Rule Exponent() {
        return Sequence(
                CharIgnoreCase('E'),
                Optional(Minus()),
                Digits()
        );
    }

    public Rule Minus() {
        return Ch('-');
    }

    @Leaf
    public Rule DotNumber() {
        return Sequence('.', Digits());
    }

    @Leaf
    public Rule Separator() {
        return ZeroOrMore(' ');
    }

    @Leaf
    public Rule Digits() {
        return OneOrMore(CharRange('0', '9'));
    }

}
