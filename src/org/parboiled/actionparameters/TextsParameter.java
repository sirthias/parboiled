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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * An ActionParameter that evaluates to a list containing the the matched input strings of the parse tree nodes
 * passed in as argument.
 * @param <V> the node value type
 */
public class TextsParameter<V> extends ActionParameterWithArgument<List<Node<V>>> {
    public TextsParameter(Object nodes) {
        super(List.class, nodes, List.class);
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        List<String> values = new ArrayList<String>();
        for (Node<V> node : resolveArgument(context)) {
            values.add(context.getNodeText(node));
        }
        return values;
    }

    @Override
    public String toString() {
        return "TEXTS(" + argument + ')';
    }
    
}
