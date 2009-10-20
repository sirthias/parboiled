package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Node;
import org.parboiled.ParsingResult;
import static org.parboiled.utils.DGraphUtils.hasChildren;
import static org.parboiled.utils.DGraphUtils.printTree;
import org.parboiled.utils.Utils;

import java.util.Collection;

public class ParseTreeUtils {

    private ParseTreeUtils() {}

    public static Node findNodeByPath(Node parent, @NotNull String path) {
        return parent != null && hasChildren(parent) ? findNodeByPath(parent.getChildren(), path) : null;
    }

    public static Node findNodeByPath(Collection<Node> nodes, @NotNull String path) {
        if (nodes != null && !nodes.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String label = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            for (Node child : nodes) {
                String childLabel = child.getLabel();
                if (childLabel.equals(label)) {
                    return separatorIndex != -1 ? findNodeByPath(child, path.substring(separatorIndex + 1)) : child;
                }
            }
        }
        return null;
    }

    public static <C extends Collection<Node>> C collectNodesByPath(Node parent, @NotNull String path,
                                                                    @NotNull C collection) {
        return parent != null && hasChildren(parent) ?
                collectNodesByPath(parent.getChildren(), path, collection) : collection;
    }

    public static <C extends Collection<Node>> C collectNodesByPath(Collection<Node> nodes, @NotNull String path,
                                                                    @NotNull C collection) {
        if (nodes != null && !nodes.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String label = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            for (Node child : nodes) {
                String childLabel = child.getLabel();
                if (childLabel.equals(label)) {
                    if (separatorIndex == -1) {
                        collection.add(child);
                    } else {
                        collectNodesByPath(child, path.substring(separatorIndex + 1), collection);
                    }
                }
            }
        }
        return collection;
    }

    public static Node findByLabel(Node searchTreeRoot, @NotNull String label) {
        if (searchTreeRoot != null) {
            if (Utils.equals(searchTreeRoot.getLabel(), label)) return searchTreeRoot;
            if (hasChildren(searchTreeRoot)) {
                Node found = findByLabel(searchTreeRoot.getChildren(), label);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static Node findByLabel(Collection<Node> nodes, @NotNull String label) {
        if (nodes != null && !nodes.isEmpty()) {
            for (Node child : nodes) {
                Node found = findByLabel(child, label);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static String getNodeText(Node node, @NotNull InputBuffer inputBuffer) {
        return node != null ? inputBuffer.extract(node.getStartLocation().index, node.getEndLocation().index) : null;
    }

    public static Character getNodeChar(Node node, InputBuffer inputBuffer) {
        return node != null && node.getEndLocation().index == node.getStartLocation().index + 1 ?
                inputBuffer.charAt(node.getStartLocation().index) : null;
    }

    public static String printNodeTree(@NotNull ParsingResult parsingResult) {
        return printTree(parsingResult.root, new NodeFormatter(parsingResult.inputBuffer));
    }

}

