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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of instructions belonging to a ACTION or Var initializer
 */
class InstructionGroup {
	
	static class VarInitGroup extends InstructionGroup {
		protected List<Type> ruleActionArgumentTypes;
		protected List<InstructionGraphNode> ruleActionArgumentNodes;
		
		public VarInitGroup(RuleMethod ruleMethod, InstructionGraphNode ruleCreationCallNode) {
			super(ruleMethod, ruleCreationCallNode, GroupType.VAR_INIT);
		}
		
		public void setRuleActionArgumentNodes(List<InstructionGraphNode> ruleActionArgumentNodes) {
			this.ruleActionArgumentNodes = ruleActionArgumentNodes;
		}
		
		public List<InstructionGraphNode> getRuleActionArgumentNodes() {
			return ruleActionArgumentNodes;
		}
		
		public void setRuleActionArgumentTypes(List<Type> ruleActionArgumentTypes) {
			this.ruleActionArgumentTypes = ruleActionArgumentTypes;
		}
		
		public List<Type> getRuleActionArgumentTypes() {
			return ruleActionArgumentTypes;
		}
	}
	
	public static enum GroupType {
		ACTION, VAR_INIT
	};

    private final List<InstructionGraphNode> nodes = new ArrayList<InstructionGraphNode>();
    private final InsnList instructions = new InsnList();
    private final RuleMethod ruleMethod;
    private final InstructionGraphNode root;
    private final List<FieldNode> fields = new ArrayList<FieldNode>();
    private String name;
    private GroupType groupType;
    private Type groupClassType;
    private byte[] groupClassCode;

	public InstructionGroup(RuleMethod ruleMethod, InstructionGraphNode root, GroupType groupType) {
		this.ruleMethod = ruleMethod;
		this.root = root;
		this.groupType = groupType;
	}

    public List<InstructionGraphNode> getNodes() {
        return nodes;
    }

    public InsnList getInstructions() {
        return instructions;
    }
    
    public RuleMethod getRuleMethod() {
		return ruleMethod;
	}

    public InstructionGraphNode getRoot() {
        return root;
    }

    public List<FieldNode> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public GroupType getGroupType() {
    	return groupType;
    }

    public Type getGroupClassType() {
        return groupClassType;
    }

    public void setGroupClassType(Type groupClassType) {
        this.groupClassType = groupClassType;
    }

    public byte[] getGroupClassCode() {
        return groupClassCode;
    }

    public void setGroupClassCode(byte[] groupClassCode) {
        this.groupClassCode = groupClassCode;
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}
