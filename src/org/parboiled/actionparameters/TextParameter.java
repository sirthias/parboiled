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

package org.parboiled.actionparameters;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Node;

/**
 * An ActionParameter that evaluates to the matched input string of the parse tree node passed in as argument.
 */
public class TextParameter implements ActionParameter {
    private final Object node;

    public TextParameter(Object node) {
        this.node = node;
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        return context.getNodeText(ActionParameterUtils.resolve(node, context, Node.class));
    }

    @Override
    public String toString() {
        return "TEXT(" + node + ')';
    }

}
