import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class NoPackageParser extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    public static class Parser extends BaseParser<Integer> {

        public Rule A() {
            return Sequence('a', push(42));
        }
    }

    @Test
    public void testNoPackageParser() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.A(), "a")
                .hasNoErrors()
                .hasParseTree("" +
                        "[A, {42}] 'a'\n" +
                        "  ['a'] 'a'\n");
    }
}