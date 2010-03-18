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

package org.parboiled.transform;

import org.jetbrains.annotations.NotNull;

/**
 * Base class of all transformers that only operate on rule that are to be rewritten (i.e. the ones containing
 * action expressions).
 */
class ActionMethodRewritingTransformer implements ClassTransformer {

    private final MethodTransformer methodTransformer;
    private final ClassTransformer nextTransformer;

    public ActionMethodRewritingTransformer(@NotNull MethodTransformer methodTransformer,
                                            ClassTransformer nextTransformer) {
        this.methodTransformer = methodTransformer;
        this.nextTransformer = nextTransformer;
    }

    @SuppressWarnings("unchecked")
    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (RuleMethod method : classNode.ruleMethods) {
            if (method.isToBeRewritten()) {
                methodTransformer.transform(classNode, method);
            }
        }
        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

}