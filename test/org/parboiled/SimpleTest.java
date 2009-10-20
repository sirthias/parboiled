package org.parboiled;

import org.testng.annotations.Test;

public class SimpleTest extends AbstractTest {

    public static class SimpleTestParser extends BaseParser<Actions> {

        public SimpleTestParser(Actions actions) {
            super(actions);
        }

        public Rule clause() {
            return sequence(digit(), operator(), digit(), eof());
        }

        public Rule operator() {
            return firstOf('+', '-');
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void test() {
        SimpleTestParser parser = Parser.create(SimpleTestParser.class, null);
        test(parser.clause(), "1+5", "" +
                "[clause] '1+5'\n" +
                "    [digit] '1'\n" +
                "    [operator] '+'\n" +
                "        ['+'] '+'\n" +
                "    [digit] '5'\n" +
                "    [eof]\n");
    }

}
