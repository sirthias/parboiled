package org.parboiled;

import static org.parboiled.TestUtils.assertEqualsMultiline;
import static org.parboiled.utils.DGraphUtils.countAllDistinct;
import static org.parboiled.utils.DGraphUtils.printTree;
import org.parboiled.utils.ToStringFormatter;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class WrappingTest extends AbstractTest {

    public static class WrappingParser extends BaseParser<Actions> {

        public WrappingParser(Actions actions) {
            super(actions);
        }

        public Rule aOpB() {
            return sequence(
                    number().label("a"),
                    operator(),
                    number().label("b")
            );
        }

        public Rule operator() {
            return firstOf('+', '-');
        }

        public Rule number() {
            return oneOrMore(digit());
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void testRecursion() {
        WrappingParser parser = Parser.create(WrappingParser.class, null);
        Rule rule = parser.aOpB();

        Matcher matcher = rule.toMatcher();
        assertEqualsMultiline(printTree(matcher, new ToStringFormatter<Matcher>()), "" +
                "aOpB\n" +
                "    wrapper:a\n" +
                "        number\n" +
                "            digit\n" +
                "    operator\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    wrapper:b\n" +
                "        number\n" +
                "            digit\n");

        // verify that number and digit matchers only exist once
        assertEquals(countAllDistinct(matcher), 8);

        test(rule, "123-54", "" +
                "[aOpB] '123-54'\n" +
                "    [a] '123'\n" +
                "        [digit] '1'\n" +
                "        [digit] '2'\n" +
                "        [digit] '3'\n" +
                "    [operator] '-'\n" +
                "        ['-'] '-'\n" +
                "    [b] '54'\n" +
                "        [digit] '5'\n" +
                "        [digit] '4'\n");
    }

}