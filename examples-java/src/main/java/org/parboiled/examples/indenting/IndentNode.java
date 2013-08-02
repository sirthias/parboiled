package org.parboiled.examples.indenting;

import java.util.ArrayList;
import java.util.List;

public class IndentNode {

	private final String name;
	private final List<IndentNode> children = new ArrayList<IndentNode>();

	public IndentNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean addChild(IndentNode child) {
		children.add(child);
		return true;
	}

	List<IndentNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "IndentNode [name=" + name + ", children=" + children + "]";
	}
}