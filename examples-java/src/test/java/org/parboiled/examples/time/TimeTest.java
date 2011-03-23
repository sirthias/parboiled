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

package org.parboiled.examples.time;

import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TimeTest {

    @Test
    public void test() {
        TimeParser parser = Parboiled.createParser(TimeParser.class);
        assertEquals(new RecoveringParseRunner(parser.Time()).run("12:00").parseTreeRoot.getValue(), "12 h, 0 min, 0 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("9:01").parseTreeRoot.getValue(), "9 h, 1 min, 0 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("02:34:56").parseTreeRoot.getValue(),
                "2 h, 34 min, 56 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("1").parseTreeRoot.getValue(), "1 h, 0 min, 0 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("12").parseTreeRoot.getValue(), "12 h, 0 min, 0 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("123").parseTreeRoot.getValue(), "1 h, 23 min, 0 s");
        assertEquals(new RecoveringParseRunner(parser.Time()).run("1234").parseTreeRoot.getValue(), "12 h, 34 min, 0 s");
        assertTrue(new RecoveringParseRunner(parser.Time()).run("12345").hasErrors());
        assertEquals(new RecoveringParseRunner(parser.Time()).run("123456").parseTreeRoot.getValue(), "12 h, 34 min, 56 s");
    }

}