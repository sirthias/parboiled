package org.parboiled;

import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class BugIn0100Test extends TestNgParboiledTest<Integer> {
    
    public interface A {
        public String get();
    }
    
    public interface B extends A {}

    public static class Parser extends BaseParser<B> {
        Rule ID() {
            return Sequence('a', match().equals(peek().get()));
        }
    }

    @Test
    public void test() {
        // throws NPE in 0.10.0
        Parboiled.createParser(Parser.class);
    }
}
