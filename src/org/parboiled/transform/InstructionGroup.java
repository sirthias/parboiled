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

import com.google.common.collect.Lists;

import java.util.List;

class InstructionGroup {

    public static final int RETURN = 1;
    public static final int CAPTURE = 2;
    public static final int ACTION = 4;

    private final List<InstructionGraphNode> nodes = Lists.newArrayList();
    private final int type;

    public InstructionGroup(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public List<InstructionGraphNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        switch(type) {
            case RETURN: return "RETURN";
            case CAPTURE: return "CAPTURE";
            case ACTION: return "ACTION";
        }
        throw new IllegalStateException();
    }
}
