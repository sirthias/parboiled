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

import com.google.common.base.Predicate;
import org.objectweb.asm.tree.MethodNode;
import org.testng.annotations.Test;

import java.util.List;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.assertEquals;

public class ClassNodeInializerTest extends TransformationTest {

    @Test
    public void testClassNodeSetup() throws Exception {
        assertEquals(classNode.name, "org/parboiled/transform/TestParser$$parboiled");
        assertEquals(classNode.superName, "org/parboiled/transform/TestParser");

        assertEqualsMultiline(join(classNode.constructors, null), "<init>");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.containsActions();
            }
        }), "RuleWithDirectExplicitAction,RuleWithIndirectExplicitAction,RuleWithIndirectExplicitDownAction," +
                "RuleWithIndirectExplicit2ParamAction,RuleWith2Returns,RuleWithCaptureInAction");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.containsCaptures();
            }
        }), "RuleWithCapture1,RuleWithCapture2,RuleWithCaptureInAction");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.containsImplicitActions();
            }
        }), "RuleWithDirectImplicitAction,RuleWithIndirectImplicitAction,RuleWithDirectImplicitUpAction," +
                "RuleWithIndirectExplicitDownAction,RuleWithIndirectImplicitParamAction," +
                "RuleWithCachedAnd2Params,RuleTakingCapture,RuleWithIllegalImplicitAction," +
                "RuleWithActionAccessingPrivateField,RuleWithActionAccessingPrivateMethod");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasCachedAnnotation();
            }
        }), "RuleWithIndirectImplicitParamAction,RuleWithIndirectExplicit2ParamAction,RuleWith2Returns," +
                "RuleWithExplicitActionsOnly,RuleWithCachedAnd2Params,RuleTakingCapture,RuleWithIllegalImplicitAction," +
                "ch,charIgnoreCase,charRange,charSet,charSet,charSet,string,string,stringIgnoreCase,stringIgnoreCase," +
                "firstOf,firstOf,oneOrMore,optional,sequence,sequence,test,testNot,zeroOrMore,fromCharLiteral," +
                "fromStringLiteral,fromCharArray,toRule");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasLabelAnnotation();
            }
        }), "RuleWithLabel,RuleWithNamedLabel,RuleWithIndirectImplicitParamAction,RuleWithIndirectExplicit2ParamAction," +
                "RuleWith2Returns,RuleWithExplicitActionsOnly,RuleWithCachedAnd2Params,RuleTakingCapture," +
                "RuleWithIllegalImplicitAction,ch,charIgnoreCase,charRange,charSet,charSet,charSet,string,string," +
                "stringIgnoreCase,stringIgnoreCase,firstOf,firstOf,oneOrMore,optional,sequence,sequence,test,testNot," +
                "zeroOrMore,fromCharLiteral,fromStringLiteral,fromCharArray,toRule");

        assertEqualsMultiline(join(classNode.ruleMethods, new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasLeafAnnotation();
            }
        }), "RuleWithLeaf,string,stringIgnoreCase");
    }

    private <T extends MethodNode> String join(List<T> methods, Predicate<T> predicate) {
        StringBuilder sb = new StringBuilder();
        for (T method : methods) {
            if (predicate == null || predicate.apply(method)) {
                if (sb.length() > 0) sb.append(',');
                sb.append(method.name);
            }
        }
        return sb.toString();
    }

}
