/*
 * Copyright (C) 2010 Reinier Zwitserloot, adapted by Mathias Doenitz
 */

package org.parboiled.examples.rpn;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

public class RpnParser extends BaseParser<Node, RpnActions> {

    public RpnParser() {
        super(Parboiled.createActions(RpnActions.class));
    }

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

    public Rule exponent() {
        return enforcedSequence(
                charIgnoreCase('E'),
                optional(minus()),
                digits()
        );
    }

    public Rule minus() {
        return ch('-');
    }

    public Rule dotNumber() {
        return enforcedSequence('.', digits());
    }

    public Rule separator() {
        return zeroOrMore(' ');
    }

    public Rule digits() {
        return oneOrMore(charRange('0', '9'));
    }

}
