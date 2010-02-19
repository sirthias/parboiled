/*
 * Copyright (C) 2009 Mathias Doenitz
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

import org.objectweb.asm.util.ASMifierClassVisitor;
import org.parboiled.Rule;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.examples.calculator.CalculatorParser;

import java.util.HashMap;

public class ASMifierHelper extends CalculatorParser {

    private HashMap<CachingGenerator.Arguments, Rule> cache;

    public class ActionWrapper implements Action<String> {

        public boolean run(Context<String> context) {
            return true;
        }

    }

    public Rule someRuleCached(String str, double d, boolean a, int b, int c, byte x) {
        if (cache == null) cache = new HashMap<CachingGenerator.Arguments, Rule>();
        return cache.get(new CachingGenerator.Arguments(new Object[] {str, d, a, b, c, x}));
    }

    public static void main(String[] args) throws Exception {
        ASMifierClassVisitor.main(new String[] {ASMifierHelper.class.getName()});
    }

}
