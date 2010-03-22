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

import org.parboiled.BaseParser;
import org.parboiled.Capture;
import org.parboiled.Rule;
import org.parboiled.support.Cached;
import org.parboiled.support.ExplicitActionsOnly;
import org.parboiled.support.Label;
import org.parboiled.support.Leaf;

@SuppressWarnings({"UnusedDeclaration"})
class TestParser extends BaseParser<Integer> {

    protected int integer;
    private int privateInt;

    public Rule RuleWithoutAction() {
        return sequence('a', 'b');
    }

    @Label
    public Rule RuleWithLabel() {
        return sequence('a', 'b');
    }

    @Label("harry")
    public Rule RuleWithNamedLabel() {
        return sequence('a', 'b');
    }

    @Leaf
    public Rule RuleWithLeaf() {
        return sequence('a', 'b');
    }

    public Rule RuleWithDirectImplicitAction() {
        return sequence('a', integer == 0, 'b');
    }

    public Rule RuleWithIndirectImplicitAction() {
        return sequence('a', 'b', action() || integer == 5);
    }

    public Rule RuleWithDirectExplicitAction() {
        return sequence('a', ACTION(action() && integer > 0), 'b');
    }

    public Rule RuleWithIndirectExplicitAction() {
        return sequence('a', 'b', ACTION(integer < 0 && action()));
    }

    public Rule RuleWithDirectImplicitUpAction() {
        return sequence('a', UP(integer) < 18, 'b');
    }

    public Rule RuleWithIndirectExplicitDownAction() {
        return sequence('a', 'b', ACTION(DOWN2(integer < 0 && action())));
    }

    public Rule RuleWithIndirectImplicitParamAction(int param) {
        return sequence('a', 'b', integer == param);
    }

    public Rule RuleWithIndirectExplicit2ParamAction(int param) {
        String string = "text";
        return sequence('a', string, ACTION(integer + param < string.length()));
    }

    public Rule RuleWithCapture1() {
        return sequence('a', 'b', RuleTakingCapture(CAPTURE(TEXT("a"))));
    }

    public Rule RuleWithCapture2() {
        Capture<String> capture = CAPTURE(TEXT("a"));
        return sequence('a', 'b', capture.get());
    }

    public Rule RuleWith2Returns(int param) {
        if (param == integer) {
            return sequence('a', ACTION(action()));
        } else {
            return eoi();
        }
    }

    @ExplicitActionsOnly
    public Rule RuleWithExplicitActionsOnly(int param) {
        Boolean b = integer == param;
        return sequence('a', 'b', b);
    }

    @Cached
    public Rule RuleWithCachedAnd2Params(String string, long aLong) {
        return sequence(string, aLong == integer);
    }

    public Rule RuleTakingCapture(Capture<String> capture) {
        return sequence('a', "harry".equals(capture.get()));
    }

    // error rules

    public Rule RuleWithIllegalImplicitAction(int param) {
        Boolean b = integer == param;
        return sequence('a', 'b', b);
    }

    public Rule RuleWithCaptureInAction() {
        return sequence('a', ACTION(integer == CAPTURE(NODES("a").size()).get()));
    }

    public Rule RuleWithActionAccessingPrivateField() {
        return sequence('a', privateInt == 0);
    }

    public Rule RuleWithActionAccessingPrivateMethod() {
        return sequence('a', privateAction());
    }

    // actions

    public boolean action() {
        return true;
    }

    private boolean privateAction() {
        return true;
    }

}
