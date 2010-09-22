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

package org.parboiled.support;

import org.parboiled.matchers.*;

/**
 * A simple matcher visitor determining whether a matcher has a custom label.
 */
public class HasCustomLabelVisitor extends DefaultMatcherVisitor<Boolean> {

    @Override
    public Boolean visit(ActionMatcher matcher) {
        return true;
    }

    @Override
    public Boolean visit(FirstOfMatcher matcher) {
        return !"FirstOf".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(OneOrMoreMatcher matcher) {
        return !"OneOrMore".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(OptionalMatcher matcher) {
        return !"Optional".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(SequenceMatcher matcher) {
        return !"Sequence".equals(matcher.getLabel()) && !"<group>".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(TestMatcher matcher) {
        return !"Test".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(TestNotMatcher matcher) {
        return !"TestNot".equals(matcher.getLabel());
    }

    @Override
    public Boolean visit(ZeroOrMoreMatcher matcher) {
        return !"ZeroOrMore".equals(matcher.getLabel());
    }

    @Override
    public Boolean defaultValue(AbstractMatcher matcher) {
        return true;
    }
}
