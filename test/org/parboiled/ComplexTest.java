package org.parboiled;

import org.testng.annotations.Test;

public class ComplexTest extends AbstractTest {

    public static class CalculatorParser extends BaseParser<CalculatorActions> {

        public CalculatorParser(CalculatorActions actions) {
            super(actions);
        }

        public Rule expression() {
            return sequence(
                    term(),
                    actions.setValue(value("term")),
                    eof().enforce()
            );
        }

        public Rule term() {
            return sequence(
                    mult(),
                    zeroOrMore(
                            firstOf('+', '-'),
                            mult().enforce()
                    ),
                    actions.compute(
                            this.<Integer>value("mult"),
                            chars("zeroOrMore/firstOf"),
                            this.<Integer>values("zeroOrMore/mult")
                    )
            );
        }

        public Rule mult() {
            return sequence(
                    atom(),
                    zeroOrMore(
                            firstOf('*', '/'),
                            atom().enforce()
                    ),
                    actions.compute(
                            this.<Integer>value("atom"),
                            chars("zeroOrMore/firstOf"),
                            this.<Integer>values("zeroOrMore/atom")
                    )
            );
        }

        public Rule atom() {
            return sequence(
                    firstOf(
                            number(),
                            sequence(
                                    '(',
                                    term().enforce(),
                                    ch(')').enforce()
                            )
                    ),
                    actions.setValue(firstNonNull(value("firstOf/number"), value("firstOf/sequence/term")))
            );
        }

        public Rule number() {
            return sequence(
                    oneOrMore(digit()),
                    actions.setValue(convertToInteger(text("oneOrMore")))
            );
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    public static abstract class CalculatorActions extends BaseActions {

        public ActionResult compute(Integer firstValue, Character[] operators, Integer[] values) {
            int value = firstValue != null ? firstValue : 0;
            for (int i = 0; i < operators.length; i++) {
                if (operators[i] != null && values[i] != null) {
                    value = performOperation(value, operators[i], values[i]);
                }
            }
            getContext().setNodeValue(value);
            return ActionResult.CONTINUE;
        }

        private int performOperation(int value1, Character operator, Integer value2) {
            switch (operator) {
                case '+':
                    return value1 + value2;
                case '-':
                    return value1 - value2;
                case '*':
                    return value1 * value2;
                case '/':
                    return value1 / value2;
            }
            throw new IllegalStateException();
        }

    }

    @Test
    public void test() {
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.create(CalculatorActions.class)
        );

        test(parser.expression().enforce(), "1+5", "" +
                "[expression, {6}] '1+5'\n" +
                "    [term, {6}] '1+5'\n" +
                "        [mult, {1}] '1'\n" +
                "            [atom, {1}] '1'\n" +
                "                [firstOf] '1'\n" +
                "                    [number, {1}] '1'\n" +
                "                        [oneOrMore] '1'\n" +
                "                            [digit] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore] '+5'\n" +
                "            [firstOf] '+'\n" +
                "                ['+'] '+'\n" +
                "            [mult, {5}] '5'\n" +
                "                [atom, {5}] '5'\n" +
                "                    [firstOf] '5'\n" +
                "                        [number, {5}] '5'\n" +
                "                            [oneOrMore] '5'\n" +
                "                                [digit] '5'\n" +
                "                [zeroOrMore]\n" +
                "    [eof]\n");
    }

}