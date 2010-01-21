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

package org.parboiled.asm;

import org.objectweb.asm.util.ASMifierClassVisitor;
import org.parboiled.AbstractAction;
import org.parboiled.Rule;
import org.parboiled.examples.calculator.CalculatorParser;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;

public class ASMifierHelper extends CalculatorParser {

    private final Integer action = 12345;
    private Rule cacheSomeRule;

    public class ActionWrapper extends AbstractAction<String> {

        public boolean run() {
            return SET(action);
        }

    }

    @Override
    public Rule empty() {
        return super.empty();
    }

    public Rule someRule() {
        return any();
    }

    @SuppressWarnings({"unchecked"})
    public Rule someRuleCached() {
        if (cacheSomeRule == null) {
            cacheSomeRule = new ProxyMatcher();

            Rule rule = any();
            if (rule instanceof AbstractMatcher && ((AbstractMatcher) rule).getLabel() == null) {
                rule.label("someRuleCached");
            }
            ((ProxyMatcher) cacheSomeRule).arm((Matcher) rule);
            cacheSomeRule = rule;
        }
        return cacheSomeRule;
    }

    public static void main(String[] args) throws Exception {
        ASMifierClassVisitor.main(new String[] {ASMifierHelper.class.getName()});
    }

}
