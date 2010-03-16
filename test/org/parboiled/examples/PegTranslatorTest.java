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

package org.parboiled.examples;

import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.examples.pegtranslator.PegTranslator;
import org.parboiled.test.FileUtils;
import org.testng.annotations.Test;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.assertTrue;

public class PegTranslatorTest {

    @Test
    public void testPegTranslator() {
        PegTranslator pegTranslator = Parboiled.createParser(PegTranslator.class, "org.parboiled.examples.PegParser");
        String pegGrammar = FileUtils.readAllTextFromResource("res/PegGrammar.test");

        // make sure we are able to translate
        assertTrue(ReportingParseRunner.run(pegTranslator.Grammar(), pegGrammar).matched);

        // make sure we translated correctly
        String testSource = FileUtils.readAllText("test/org/parboiled/examples/PegParser.java");
        assertEqualsMultiline(pegTranslator.getSource(), testSource);

        // make sure the translation works
        PegParser pegParser = Parboiled.createParser(PegParser.class);
        assertTrue(ReportingParseRunner.run(pegParser.Grammar(), pegGrammar).matched);
    }

}
