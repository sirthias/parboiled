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

package org.parboiled.transform;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;
import org.parboiled.annotations.*;

import static java.lang.Integer.parseInt;
import static org.parboiled.common.StringUtils.isEmpty;

@SuppressWarnings({"UnusedDeclaration"})
@BuildParseTree
class TestParser extends BaseParser<Integer> {

    protected int integer;
    private int privateInt;

    public Rule RuleWithoutAction() {
        return Sequence('a', 'b');
    }

    @Label("harry")
    public Rule RuleWithNamedLabel() {
        return Sequence('a', 'b');
    }

    @SuppressNode
    public Rule RuleWithLeaf() {
        return Sequence('a', 'b');
    }

    public Rule RuleWithDirectImplicitAction() {
        return Sequence('a', integer == 0, 'b', 'c');
    }

    public Rule RuleWithIndirectImplicitAction() {
        return Sequence('a', 'b', action() || integer == 5);
    }

    public Rule RuleWithDirectExplicitAction() {
        return Sequence('a', ACTION(action() && integer > 0), 'b');
    }

    public Rule RuleWithIndirectExplicitAction() {
        return Sequence('a', 'b', ACTION(integer < 0 && action()));
    }

    public Rule RuleWithIndirectImplicitParamAction(int param) {
        return Sequence('a', 'b', integer == param);
    }

    public Rule RuleWithComplexActionSetup(int param) {
        int i = 26, j = 18;
        Var<String> string = new Var<String>("text");
        i += param;
        j -= i;
        return Sequence('a' + i, i > param + j, string, ACTION(integer + param < string.get().length() - i - j));
    }

    public Rule BugIn0990() {
        Var<Integer> var = new Var<Integer>();
        return FirstOf("10", "2");
    }

    @DontLabel
    public Rule RuleWith2Returns(int param) {
        if (param == integer) {
            return Sequence('a', ACTION(action()));
        } else {
            return EOI;
        }
    }

    @ExplicitActionsOnly
    public Rule RuleWithExplicitActionsOnly(int param) {
        Boolean b = integer == param;
        return Sequence('a', 'b', b);
    }

    @Cached
    public Rule RuleWithCachedAnd2Params(String string, long aLong) {
        return Sequence(string, aLong == integer);
    }

    public Rule RuleWithFakeImplicitAction(int param) {
        Boolean b = integer == param;
        return Sequence('a', 'b', b);
    }

    public Rule NumberRule() {
        return Sequence(
                OneOrMore(CharRange('0', '9')).suppressNode(),
                push(parseInt(isEmpty(match()) ? "0" : match()))
        );
    }

    // actions

    public boolean action() {
        return true;
    }

}
