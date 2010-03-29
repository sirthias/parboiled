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

import static java.lang.Integer.parseInt;
import static org.parboiled.common.StringUtils.isEmpty;

@SuppressWarnings({"UnusedDeclaration"})
class TestParser extends BaseParser<Integer> {

    protected int integer;
    private int privateInt;

    public Rule RuleWithoutAction() {
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
        return sequence('a', integer == 0, 'b', 'c');
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
        return sequence('a', UP2(SET(integer)), 'b');
    }

    public Rule RuleWithIndirectExplicitDownAction() {
        return sequence('a', 'b', UP3(DOWN2(integer < 0 && action())));
    }

    public Rule RuleWithIndirectImplicitParamAction(int param) {
        return sequence('a', 'b', integer == param);
    }

    public Rule RuleWithComplexActionSetup(int param) {
        int i = 26, j = 18;
        String string = "text";
        i += param;
        j -= i;
        return sequence('a' + i, i > param + j, string, ACTION(integer + param < string.length() - i - j));
    }

    public Rule RuleWithCapture() {
        return sequence('a', 'b', RuleWithCaptureParameter(CAPTURE(TEXT("a"))));
    }

    public Rule RuleWithCaptureParameter(Capture<String> capture) {
        return sequence('a', "harry".equals(capture.get()));
    }

    @Label
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

    public Rule RuleWithFakeImplicitAction(int param) {
        Boolean b = integer == param;
        return sequence('a', 'b', b);
    }

    public Rule NumberRule() {
        return sequence(
                oneOrMore(charRange('0', '9')).asLeaf(),
                SET(parseInt(isEmpty(LAST_TEXT()) ? "0" : LAST_TEXT()))
        );
    }

    // actions

    public boolean action() {
        return true;
    }

}
