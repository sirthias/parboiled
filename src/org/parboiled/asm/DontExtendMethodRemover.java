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

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.jetbrains.annotations.NotNull;

public class DontExtendMethodRemover implements ClassTransformer {
    private final ClassTransformer nextTransformer;

    public DontExtendMethodRemover(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (int i = 0; i < classNode.methods.size(); i++) {
            MethodNode method = (MethodNode) classNode.methods.get(i);
            if (carriesDontExtendAnnotation(method)) {
                classNode.methods.remove(i--);
            }
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private boolean carriesDontExtendAnnotation(MethodNode method) {
        if (method.visibleAnnotations != null) {
            for (Object annotationObj : method.visibleAnnotations) {
                AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(AsmUtils.DONT_EXTEND_ANNOTATION_TYPE.getDescriptor())) return true;
            }
        }
        return false;
    }

}
