/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

import static org.parboiled.common.Utils.arrayOf;
import org.parboiled.matchers.FirstOfMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.test.AbstractTest;
import static org.parboiled.trees.GraphUtils.countAllDistinct;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class CachingTest extends AbstractTest {

    public static class CachingParser extends BaseParser<Object> {

        public Rule rule1() {
            return sequence(
                    firstOf('+', '-'),
                    digit(),
                    firstOf('+', '-'),
                    digit()
            );
        }

        public Rule rule2() {
            return sequence(
                    firstOf_uncached('+', '-'),
                    digit(),
                    firstOf_uncached('+', '-'),
                    digit()
            );
        }

        public Rule digit() {
            return charRange('0', '9');
        }

        public Rule firstOf_uncached(Object rule, Object rule2, Object... moreRules) {
            return new FirstOfMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules)))).label("firstOf");
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLabellingParser() {
        CachingParser parser = Parboiled.createParser(CachingParser.class);

        Matcher matcher1 = (Matcher) parser.rule1();
        Matcher matcher2 = (Matcher) parser.rule2();

        assertEquals(countAllDistinct(matcher1), 5);
        assertEquals(countAllDistinct(matcher2), 6);
    }

}