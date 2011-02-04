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

package org.parboiled.parserunners;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class RecoveryErrorActionsTest {

    public static class Parser extends BaseParser<Object> {

        Rule Clause() {
            return Sequence(Seq(), EOI);
        }

        Rule Seq() {
            return Sequence(A(), B(), C(), D());
        }

        Rule A() {
            return Sequence('a', push(match()));
        }

        Rule B() {
            return Sequence('b', push(match()));
        }

        Rule C() {
            return Sequence('c', push(1));
        }

        Rule D() {
            return Sequence('d', push(2.0));
        }
    }

    @Test
    public void testRecoveryErrorActions1() {
        Parser parser = Parboiled.createParser(Parser.class);

        ParsingResult<?> result = new RecoveringParseRunner(parser.Clause()).run("abcd");
        assertFalse(result.hasErrors());
        assertEquals(toList(result.valueStack), Arrays.asList(2.0, 1, "b", "a"));
    }

    @Test
    public void testRecoveryErrorActions2() {
        Parser parser = Parboiled.createParser(Parser.class);

        ParsingResult<?> result = new RecoveringParseRunner(parser.Clause()).run("axx");
        assertEquals(printParseErrors(result), "" +
                "Invalid input 'x...', expected B (line 1, pos 2):\n" +
                "axx\n" +
                " ^^\n");
        assertEquals(toList(result.valueStack), Arrays.asList(2.0, 1, "", "a"));
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T t : iterable) list.add(t);
        return list;
    }

}