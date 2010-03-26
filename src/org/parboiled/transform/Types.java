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

import org.objectweb.asm.Type;
import org.parboiled.*;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;
import org.parboiled.support.*;

interface Types {

    static final Type ABSTRACT_MATCHER = Type.getType(AbstractMatcher.class);
    static final Type ACTION = Type.getType(Action.class);
    static final Type BASE_ACTION = Type.getType(BaseAction.class);
    static final Type BASE_CAPTURE = Type.getType(BaseCapture.class);
    static final Type BASE_PARSER = Type.getType(BaseParser.class);
    static final Type CACHED = Type.getType(Cached.class);
    static final Type CAPTURE = Type.getType(Capture.class);
    static final Type CONTEXT_AWARE = Type.getType(ContextAware.class);
    static final Type CONTEXT = Type.getType(Context.class);
    static final Type DONT_LABEL = Type.getType(DontLabel.class);
    static final Type EXPLICIT_ACTIONS_ONLY = Type.getType(ExplicitActionsOnly.class);
    static final Type LABEL = Type.getType(Label.class);
    static final Type LEAF = Type.getType(Leaf.class);
    static final Type MATCHER = Type.getType(Matcher.class);
    static final Type PROXY_MATCHER = Type.getType(ProxyMatcher.class);
    static final Type RULE = Type.getType(Rule.class);

    static final String ACTION_DESC = ACTION.getDescriptor();
    static final String CACHED_DESC = CACHED.getDescriptor();
    static final String CONTEXT_DESC = CONTEXT.getDescriptor();
    static final String DONT_LABEL_DESC = DONT_LABEL.getDescriptor();
    static final String EXPLICIT_ACTIONS_ONLY_DESC = EXPLICIT_ACTIONS_ONLY.getDescriptor();
    static final String LABEL_DESC = LABEL.getDescriptor();
    static final String LEAF_DESC = LEAF.getDescriptor();
    static final String MATCHER_DESC = MATCHER.getDescriptor();
    static final String RULE_DESC = RULE.getDescriptor();
}
