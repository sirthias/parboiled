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

import org.objectweb.asm.tree.MethodNode;
import org.parboiled.common.Predicate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

public class ClassNodeInializerTest extends TransformationTest {

    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test(enabled = false)
    public void testClassNodeSetup() throws Exception {
        assertEquals(classNode.name, "org/parboiled/transform/TestParser$$parboiled");
        assertEquals(classNode.superName, "org/parboiled/transform/TestParser");

        assertEquals(join(classNode.getConstructors(), null), "<init>");

        assertEquals(join(classNode.getRuleMethods().values(), new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.containsExplicitActions();
            }
        }), "RuleWithDirectExplicitAction,RuleWithIndirectExplicitAction,RuleWithIndirectExplicitDownAction," +
                "RuleWithIndirectExplicit2ParamAction,RuleWith2Returns,RuleWithCaptureInAction");

        assertEquals(join(classNode.getRuleMethods().values(), new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.containsImplicitActions();
            }
        }), "RuleWithDirectImplicitAction,RuleWithIndirectImplicitAction,RuleWithDirectImplicitUpAction," +
                "RuleWithIndirectExplicitDownAction,RuleWithIndirectImplicitParamAction," +
                "RuleWithCachedAnd2Params,RuleWithCaptureParameter,RuleWithIllegalImplicitAction," +
                "RuleWithActionAccessingPrivateField,RuleWithActionAccessingPrivateMethod");

        assertEquals(join(classNode.getRuleMethods().values(), new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasCachedAnnotation();
            }
        }), "RuleWithoutAction,RuleWithLabel,RuleWithNamedLabel,RuleWithLeaf,RuleWithDirectImplicitAction," +
                "RuleWithIndirectImplicitAction,RuleWithDirectExplicitAction,RuleWithIndirectExplicitAction," +
                "RuleWithDirectImplicitUpAction,RuleWithIndirectExplicitDownAction,RuleWithCapture1,RuleWithCapture2," +
                "RuleWithCachedAnd2Params,RuleWithCaptureInAction,RuleWithActionAccessingPrivateField," +
                "RuleWithActionAccessingPrivateMethod,Ch,IgnoreCase,CharRange,AnyOf,String,IgnoreCase," +
                "FirstOf,OneOrMore,Optional,Sequence,Test,TestNot,ZeroOrMore,Eoi,Any,Empty");

        assertEquals(join(classNode.getRuleMethods().values(), new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasDontLabelAnnotation();
            }
        }), "RuleWithoutAction,RuleWithLabel,RuleWithNamedLabel,RuleWithLeaf,RuleWithDirectImplicitAction," +
                "RuleWithIndirectImplicitAction,RuleWithDirectExplicitAction,RuleWithIndirectExplicitAction," +
                "RuleWithDirectImplicitUpAction,RuleWithIndirectExplicitDownAction,RuleWithCapture1,RuleWithCapture2," +
                "RuleWithCaptureInAction,RuleWithActionAccessingPrivateField,RuleWithActionAccessingPrivateMethod," +
                "Eoi,Any,Empty");

        assertEquals(join(classNode.getRuleMethods().values(), new Predicate<RuleMethod>() {
            public boolean apply(RuleMethod method) {
                return method.hasSuppressNodeAnnotation();
            }
        }), "RuleWithLeaf");
    }

    private <T extends MethodNode> String join(Collection<T> methods, Predicate<T> predicate) {
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
