package org.parboiled;

import org.testng.annotations.Test;

@SuppressWarnings({"InfiniteRecursion"})
public class AbcnTest extends AbstractTest {

    /**
     * The classic non-context free language example { a^n b^n c^n : n >= 1 }
     * S ← &(A c) a+ B !(a/b/c)
     * A ← a A? b
     * B ← b B? c
     */
    public static class TestParser extends BaseParser<Actions> {

        public TestParser(Actions actions) {
            super(actions);
        }

        public Rule S() {
            return sequence(
                    test(sequence(A(), 'c')),
                    oneOrMore('a'),
                    B(),
                    testNot(firstOf('a', 'b', 'c'))
            );
        }

        public Rule A() {
            return sequence('a', optional(A()), 'b');
        }

        public Rule B() {
            return sequence('b', optional(B()), 'c');
        }

    }

    @Test
    public void test() {
        TestParser parser = Parser.create(TestParser.class, null);
        test(parser.S(), "aabbcc", "" +
                "[S] 'aabbcc'\n" +
                "    [oneOrMore] 'aa'\n" +
                "        ['a'] 'a'\n" +
                "        ['a'] 'a'\n" +
                "    [B] 'bbcc'\n" +
                "        ['b'] 'b'\n" +
                "        [optional] 'bc'\n" +
                "            [B] 'bc'\n" +
                "                ['b'] 'b'\n" +
                "                [optional]\n" +
                "                ['c'] 'c'\n" +
                "        ['c'] 'c'\n");
    }

    @Test
    public void testFail() {
        TestParser parser = Parser.create(TestParser.class, null);
        testFail(parser.S(), "aabbbcc", "" +
                "[S] 'aabbbcc'\n" +
                "    [!ILLEGAL!] 'a'\n" +
                "    [&(sequence)]\n" +
                "    [oneOrMore] 'a'\n" +
                "        ['a'] 'a'\n" +
                "    [B] 'bbbcc'\n" +
                "        ['b'] 'b'\n" +
                "        [optional] 'bbcc'\n" +
                "            [B] 'bbcc'\n" +
                "                ['b'] 'b'\n" +
                "                [optional] 'bc'\n" +
                "                    [B] 'bc'\n" +
                "                        ['b'] 'b'\n" +
                "                        [optional]\n" +
                "                        ['c'] 'c'\n" +
                "                ['c'] 'c'\n" +
                "        ['c']\n", "" +
                "ParseError: Invalid input 'a', expected sequence (line 1, pos 1):\n" +
                "aabbbcc\n" +
                "^\n" +
                "---\n" +
                "ParseError: Invalid input 'a', expected sequence (line 1, pos 2):\n" +
                "aabbbcc\n" +
                " ^\n" +
                "---\n" +
                "ParseError: Invalid input EOF, expected 'c' (line 1, pos 8):\n" +
                "aabbbcc\n" +
                "       ^\n"
        );
    }

}