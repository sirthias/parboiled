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

package org.parboiled.examples.java;

import org.parboiled.parserunners.ProfilingParseRunner;
import org.parboiled.Rule;
import org.parboiled.support.ParsingResult;

public class JavaParserProfiler extends Main {

    private ProfilingParseRunner parseRunner;

    public static void main(String[] args) {
        new JavaParserProfiler().run(args);
    }

    @Override
    protected void run(String[] args) {
        super.run(args);
        ProfilingParseRunner.Report report = parseRunner.getReport();
        System.out.println();
        System.out.println(report.print());
    }
    @Override
    protected ParsingResult<?> run(Rule rootRule, String sourceText) {
        if (parseRunner == null) {
            parseRunner = new ProfilingParseRunner(rootRule);
        }
        return parseRunner.run(sourceText);
    }

}
