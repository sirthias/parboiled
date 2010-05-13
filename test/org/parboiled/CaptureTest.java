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

import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class CaptureTest extends AbstractTest {

    public static class Parser extends BaseParser<Integer> {

        public Rule A() {
            return Sequence('a', set(1), B(CAPTURE(value())));
        }

        public Rule B(Capture<Integer> captureA) {
            return Sequence('b', set(2), C(captureA, CAPTURE(UP(value())), CAPTURE(value())));
        }

        public Rule C(Capture<Integer> captureA, Capture<Integer> captureBUp1, Capture<Integer> captureB) {
            return Sequence('c', set(3),
                    testCaptures(captureA, captureBUp1, captureB, CAPTURE(UP2(value())), CAPTURE(UP(value())),
                            CAPTURE(value())));
        }

        @SuppressWarnings({"PointlessBooleanExpression"})
        public Rule testCaptures(Capture<Integer> captureA, Capture<Integer> captureBUp1, Capture<Integer> captureB,
                                 Capture<Integer> captureCUp2, Capture<Integer> captureCUp1,
                                 Capture<Integer> captureC) {
            return ToRule(true &&
                    captureA.get() == 1 &&
                    captureBUp1.get() == 1 &&
                    captureB.get() == 2 &&
                    captureCUp2.get() == 1 &&
                    captureCUp1.get() == 2 &&
                    captureC.get() == 3
            );
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.A(), "abc", "" +
                "[A, {1}] 'abc'\n" +
                "    ['a'] 'a'\n" +
                "    [B, {2}] 'bc'\n" +
                "        ['b'] 'b'\n" +
                "        [C, {3}] 'c'\n" +
                "            ['c'] 'c'\n");

    }

}