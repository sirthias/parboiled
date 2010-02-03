/*
 * Copyright (C) 2010 Reinier Zwitserloot, adapted by Mathias Doenitz
 */

package org.parboiled.examples.rpn;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Leaf;

public class RpnParser extends BaseParser<Node> {

    public final RpnActions actions = new RpnActions();

    public Rule operation() {
        return sequence(
                separator(),
                zeroOrMore(atom()).label("sequenceOfAtoms"),
                eoi(),
                SET(actions.runStack(VALUES("s/a"))));
    }

    public Rule atom() {
        return firstOf(number(), binarySymbol());
    }

    public Rule binarySymbol() {
        return sequence(
                firstOf('+', '-', '*', '/', '^'),
                SET(actions.toOperator(LAST_CHAR())),
                separator()
        );
    }

    public Rule number() {
        return sequence(
                sequence(
                        optional(minus()),
                        firstOf(
                                dotNumber(),
                                sequence(digits(), optional(dotNumber()))
                        ),
                        optional(exponent())
                ),
                SET(actions.toBigDecimal(LAST_TEXT())),
                separator()
        );
    }

    @Leaf
    public Rule exponent() {
        return sequence(
                charIgnoreCase('E'),
                optional(minus()),
                digits()
        );
    }

    public Rule minus() {
        return ch('-');
    }

    @Leaf
    public Rule dotNumber() {
        return sequence('.', digits());
    }

    @Leaf
    public Rule separator() {
        return zeroOrMore(' ');
    }

    @Leaf
    public Rule digits() {
        return oneOrMore(charRange('0', '9'));
    }

}
