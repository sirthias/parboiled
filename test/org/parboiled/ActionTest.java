package org.parboiled;

import org.testng.annotations.Test;

public class ActionTest extends AbstractTest {

    public static class ActionTestParser extends BaseParser<BaseActions> {

        public ActionTestParser(BaseActions actions) {
            super(actions);
        }

        public Rule number() {
            return sequence(
                    oneOrMore(digit()),
                    actions.setValue(convertToNumber(text("oneOrMore"), Integer.class))
            );
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void test() {
        ActionTestParser parser = Parser.create(ActionTestParser.class, Parser.create(BaseActions.class));
        test(parser.number(), "123", "" +
                "[number, {123}] '123'\n" +
                "    [oneOrMore] '123'\n" +
                "        [digit] '1'\n" +
                "        [digit] '2'\n" +
                "        [digit] '3'\n");
    }

}