/*
 * Copyright (C) 2026 parboiled contributors
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

package org.parboiled.transform;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class LookupArgumentTest extends TestNgParboiledTest<Integer> {

    public static class Parser extends BaseParser<Integer> {

        public Rule A() {
            return Sequence('a', ACTION(push(42)));
        }
    }

    @Test
    public void testLookupArgumentUsed() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Parser parser = Parboiled.createParser(lookup, Parser.class);
        Assert.assertSame(AsmUtils.lookupFor(Parser.class), lookup,
                "The lookup passed to createParser() must be used.");
        test(parser.A(), "a").hasNoErrors();
    }

    @Test
    public void testLookupWithoutPackageAccessRejected() {
        LookupFactory lookupFactory = new LookupFactory();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> lookupFactory.registerLookup(Parser.class, MethodHandles.publicLookup()));
    }

    @Test
    public void testLookupFromOtherPackageRejected() {
        LookupFactory lookupFactory = new LookupFactory();
        MethodHandles.Lookup otherPackageLookup = MethodHandles.lookup().in(Parboiled.class);
        Assert.assertThrows(IllegalArgumentException.class,
                () -> lookupFactory.registerLookup(Parser.class, otherPackageLookup));
    }
}
