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

import java.util.ArrayList;
import java.util.List;

/**
 * An ActionParameter that evaluates to the values set on the parse tree nodes passed in as argument.
 * @param <V> the node value type
 */
public class ValuesParameter<V> extends ActionParameterWithArgument<List<Node<V>>> {
    
    public ValuesParameter(Object nodes) {
        super(List.class, nodes, List.class);
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        List<V> values = new ArrayList<V>();
        for (Node<V> node : resolveArgument(context)) {
            values.add(node.getValue());
        }
        return values;
    }

    @Override
    public String toString() {
        return "VALUES(" + argument + ')';
    }

}
