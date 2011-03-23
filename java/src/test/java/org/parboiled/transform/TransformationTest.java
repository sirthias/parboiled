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

import java.io.IOException;
import java.util.List;

abstract class TransformationTest {

    protected ParserClassNode classNode;

    public void setup(Class<?> parserClass) throws IOException {
        classNode = new ParserClassNode(parserClass);
        new ClassNodeInitializer().process(classNode);
    }

    public RuleMethod processMethod(String methodName, List<RuleMethodProcessor> methodProcessors) throws Exception {
        RuleMethod method = method(methodName);
        for (RuleMethodProcessor processor : methodProcessors) {
            if (processor.appliesTo(classNode, method)) {
                processor.process(classNode, method);
            }
        }
        return method;
    }

    public RuleMethod method(String name) {
        for (RuleMethod method : classNode.getRuleMethods().values()) {
            if (name.equals(method.name)) return method;
        }
        throw new IllegalArgumentException("Method '" + name + "' not found");
    }
}