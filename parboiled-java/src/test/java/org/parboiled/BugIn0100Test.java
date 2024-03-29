/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public void testBugIn0100() {
        // throws NPE in 0.10.0
        Parboiled.createParser(Parser.class);
    }
}
