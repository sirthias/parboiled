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

import org.parboiled.Node;
import org.parboiled.MatcherContext;
import org.parboiled.common.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * An ActionParameter that evaluates to a list containing the the matched input strings of the parse tree nodes
 * passed in as argument.
 */
public class TextsParameter implements ActionParameter {
    private final Object nodes;

    public TextsParameter(Object nodes) {
        this.nodes = nodes;
    }

    public Object resolve(@NotNull MatcherContext<?> context) throws Throwable {
        List<String> values = new ArrayList<String>();
        for (Object node : ActionParameterUtils.resolve(nodes, context, List.class)) {
            Preconditions.checkArgument(node instanceof Node);
            values.add(context.getNodeText((Node) node));
        }
        return values;
    }

    @Override
    public String toString() {
        return "TEXTS(" + nodes + ')';
    }
    
}
