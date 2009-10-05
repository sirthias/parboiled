package org.parboiled;

import org.testng.annotations.Test;

public class RecursionTest extends AbstractTest {

    public static class RecursionParser extends BaseParser<Actions> {

        public RecursionParser(Actions actions) {
            super(actions);
        }

        @SuppressWarnings({"InfiniteRecursion"})
        public Rule lotsOfAs() {
            return sequence('A', optional(lotsOfAs()));
        }

    }

    @Test
    public void testRecursion() {
        RecursionParser parser = Parser.create(RecursionParser.class, null);
        test(parser.lotsOfAs(), "AAA", "" +
                "[lotsOfAs] 'AAA'\n" +
                "    ['A'] 'A'\n" +
                "    [optional] 'AA'\n" +
                "        [lotsOfAs] 'AA'\n" +
                "            ['A'] 'A'\n" +
                "            [optional] 'A'\n" +
                "                [lotsOfAs] 'A'\n" +
                "                    ['A'] 'A'\n" +
                "                    [optional]\n");
    }

}