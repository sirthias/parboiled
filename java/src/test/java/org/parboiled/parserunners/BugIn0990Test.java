package org.parboiled.parserunners;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class BugIn0990Test extends TestNgParboiledTest<Integer> {

    static class Parser extends BaseParser<Integer> {
        Rule Clause() {
            return Sequence(Id(), ZeroOrMore('.', Id()), ';');
        }

        Rule Id() {
            return OneOrMore(TestNot('.'), ANY);
        }
    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);

        // threw IllegalStateException in 0.9.9.0
        testWithRecovery(parser.Clause(), "a.b;")
                .hasErrors("" +
                        "Invalid input 'EOI', expected '.', ANY or ';' (line 1, pos 5):\n" +
                        "a.b;\n" +
                        "    ^\n");
    }
}
