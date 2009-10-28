package org.parboiled;

import static org.parboiled.TestUtils.assertEqualsMultiline;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import org.parboiled.utils.StringUtils;
import static org.testng.Assert.fail;

abstract class AbstractTest {

    public void test(Rule rule, String input, String expectedTree) {
        ParsingResult parsingResult = Parser.parse(rule, input);
        if (!parsingResult.parseErrors.isEmpty()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult)
            );
        }

        String actualTree = printNodeTree(parsingResult);
        assertEqualsMultiline(actualTree, expectedTree);
    }

    public void testFail(Rule rule, String input, String expectedTree, String expectedErrors) {
        ParsingResult parsingResult = Parser.parse(rule, input);
        String actualTree = printNodeTree(parsingResult);
        assertEqualsMultiline(actualTree, expectedTree);
        assertEqualsMultiline(StringUtils.join(parsingResult.parseErrors, "---\n"), expectedErrors);
    }

}