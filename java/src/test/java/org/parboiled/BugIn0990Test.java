package org.parboiled;

import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class BugIn0990Test extends TestNgParboiledTest<Integer> {

    public static class Parser extends BaseParser<Integer> {
        Rule ID() {
            return Sequence('a', WhiteSpaceChar(), 'b');
        }

        Rule WhiteSpaceChar() {
            return AnyOf(" \n\r\t\f");
        }
    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.ID(), "ab")
                .hasErrors("" +
                        "Invalid input 'b', expected WhiteSpaceChar (line 1, pos 2):\n" +
                        "ab\n" +
                        " ^\n");
    }
}
