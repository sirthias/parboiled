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

import org.objectweb.asm.Type;
import org.parboiled.*;
import org.parboiled.annotations.*;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;
import org.parboiled.matchers.VarFramingMatcher;
import org.parboiled.support.Var;

interface Types {
    static final Type BASE_ACTION = Type.getType(BaseAction.class);
    static final Type BASE_VAR_INIT = Type.getType(BaseVarInit.class);
    static final Type BASE_PARSER = Type.getType(BaseParser.class);
    static final Type CONTEXT_AWARE = Type.getType(ContextAware.class);
    static final Type LABEL = Type.getType(Label.class);
    static final Type MATCHER = Type.getType(Matcher.class);
    static final Type PROXY_MATCHER = Type.getType(ProxyMatcher.class);
    static final Type RULE = Type.getType(Rule.class);
    static final Type VAR = Type.getType(Var.class);
    static final Type VAR_FRAMING_MATCHER = Type.getType(VarFramingMatcher.class);

    static final String ACTION_DESC = Type.getType(Action.class).getDescriptor();
    static final String CACHED_DESC = Type.getType(Cached.class).getDescriptor();
    static final String CONTEXT_DESC = Type.getType(Context.class).getDescriptor();
    static final String DONT_EXTEND_DESC = Type.getType(DontExtend.class).getDescriptor();
    static final String DONT_LABEL_DESC = Type.getType(DontLabel.class).getDescriptor();
    static final String EXPLICIT_ACTIONS_ONLY_DESC = Type.getType(ExplicitActionsOnly.class).getDescriptor();
    static final String LABEL_DESC = LABEL.getDescriptor();
    static final String SUPPRESS_NODE_DESC = Type.getType(SuppressNode.class).getDescriptor();
    static final String SUPPRESS_SUBNODES_DESC = Type.getType(SuppressSubnodes.class).getDescriptor();
    static final String SKIP_ACTIONS_IN_PREDICATES_DESC = Type.getType(SkipActionsInPredicates.class).getDescriptor();
    static final String DONT_SKIP_ACTIONS_IN_PREDICATES_DESC = Type.getType(DontSkipActionsInPredicates.class).getDescriptor();
    static final String BUILD_PARSE_TREE_DESC = Type.getType(BuildParseTree.class).getDescriptor();
    static final String SKIP_NODE_DESC = Type.getType(SkipNode.class).getDescriptor();
    static final String MEMO_MISMATCHES_DESC = Type.getType(MemoMismatches.class).getDescriptor();
    static final String MATCHER_DESC = MATCHER.getDescriptor();
    static final String RULE_DESC = RULE.getDescriptor();
    static final String VAR_DESC = VAR.getDescriptor();
}
