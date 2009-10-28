package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.Converter;
import static org.parboiled.support.ParseTreeUtils.collectNodesByLabel;
import static org.parboiled.support.ParseTreeUtils.collectNodesByPath;
import org.parboiled.utils.Preconditions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates parameters passed to parser actions.
 */
abstract class ActionParameter {

    // the type of the action method parameter that is to be provided by this instance
    protected Class<?> expectedParameterType;

    public void setExpectedType(Class<?> parameterType) {
        expectedParameterType = parameterType;
    }

    abstract Object getValue(@NotNull MatcherContext context);

    //////////////////////////////////// SPECIALIZATION //////////////////////////////////////////

    /**
     * The base class of all ActionParameters that operate on Node paths.
     */
    abstract static class PathBasedActionParameter extends ActionParameter {
        protected final String path;

        protected PathBasedActionParameter(String path) {
            this.path = path;
        }

        protected ArrayList<org.parboiled.Node> collectPathNodes(MatcherContext context) {
            return collectNodesByPath(context.getSubNodes(), path, new ArrayList<org.parboiled.Node>());
        }
    }

    //////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////

    static class Node extends PathBasedActionParameter {
        public Node(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Checks.ensure(expectedParameterType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node.class);
            Preconditions.checkState(expectedParameterType.isAssignableFrom(org.parboiled.Node.class));
            return context.getNodeByPath(path);
        }
    }

    static class Nodes extends PathBasedActionParameter {
        public Nodes(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Checks.ensure(expectedParameterType.isArray() && componentType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node[].class);
            List<org.parboiled.Node> list = collectPathNodes(context);
            return list.toArray((org.parboiled.Node[]) Array.newInstance(componentType, list.size()));
        }
    }

    static class NodeWithLabel extends PathBasedActionParameter {
        public NodeWithLabel(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Checks.ensure(expectedParameterType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node.class);
            Preconditions.checkState(expectedParameterType.isAssignableFrom(org.parboiled.Node.class));
            return context.getNodeByLabel(path);
        }
    }

    static class NodesWithLabel extends PathBasedActionParameter {
        public NodesWithLabel(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Checks.ensure(expectedParameterType.isArray() && componentType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getPath(), expectedParameterType, org.parboiled.Node[].class);
            List<org.parboiled.Node> list = collectNodesByLabel(context.getSubNodes(), path,
                    new ArrayList<org.parboiled.Node>());
            return list.toArray((org.parboiled.Node[]) Array.newInstance(componentType, list.size()));
        }
    }

    static class Value extends PathBasedActionParameter {
        public Value(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            org.parboiled.Node node = context.getNodeByPath(path);
            if (node == null || node.getValue() == null) return null;
            Object value = node.getValue();
            Checks.ensure(expectedParameterType.isAssignableFrom(value.getClass()),
                    "Illegal action argument in '%s', cannot cast %s to %s",
                    context.getPath(), value.getClass(), expectedParameterType);
            return value;
        }
    }

    static class Values extends PathBasedActionParameter {
        public Values(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Preconditions.checkState(expectedParameterType.isArray() && !componentType.isPrimitive());
            List<org.parboiled.Node> nodes = collectPathNodes(context);
            Object array = Array.newInstance(componentType, nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                Object value = nodes.get(i).getValue();
                if (value == null) continue;
                Checks.ensure(componentType.isAssignableFrom(value.getClass()),
                        "Illegal action argument in '%s', cannot cast value array component from %s to %s",
                        context.getPath(), value.getClass(), componentType);
                Array.set(array, i, value);
            }
            return array;
        }
    }

    static class Text extends PathBasedActionParameter {
        public Text(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(String.class));
            org.parboiled.Node node = context.getNodeByPath(path);
            return context.getNodeText(node);
        }
    }

    static class Texts extends PathBasedActionParameter {
        public Texts(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == String.class);
            List<org.parboiled.Node> nodes = collectPathNodes(context);
            String[] texts = new String[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                texts[i] = context.getNodeText(nodes.get(i));
            }
            return texts;
        }
    }

    static class Char extends PathBasedActionParameter {
        public Char(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(Character.class));
            org.parboiled.Node node = context.getNodeByPath(path);
            return context.getNodeChar(node);
        }
    }

    static class Chars extends PathBasedActionParameter {
        public Chars(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == Character.class);
            List<org.parboiled.Node> nodes = collectPathNodes(context);
            Character[] chars = new Character[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                chars[i] = context.getNodeChar(nodes.get(i));
            }
            return chars;
        }
    }

    static class FirstOfNonNull extends ActionParameter {
        private final Object[] args;

        public FirstOfNonNull(@NotNull Object[] args) {
            this.args = args;
        }

        @Override
        public void setExpectedType(Class<?> parameterType) {
            super.setExpectedType(parameterType);
            for (Object arg : args) {
                if (arg instanceof ActionParameter) {
                    ((ActionParameter) arg).setExpectedType(parameterType);
                }
            }
        }

        Object getValue(@NotNull MatcherContext context) {
            for (Object arg : args) {
                if (arg instanceof ActionParameter) {
                    ActionParameter param = (ActionParameter) arg;
                    arg = param.getValue(context);
                }
                if (arg != null) {
                    Checks.ensure(expectedParameterType.isAssignableFrom(arg.getClass()),
                            "Illegal action argument in firstOfNonNull(...) in '%s', cannot cast %s to %s",
                            context.getPath(), arg.getClass(), expectedParameterType);
                    return arg;
                }
            }
            return null;
        }
    }

    static class Convert<T> extends ActionParameter {
        private final Object arg;
        private final Converter<T> converter;

        public Convert(Object arg, @NotNull Converter<T> converter) {
            this.arg = arg;
            this.converter = converter;
        }

        @Override
        public void setExpectedType(Class<?> parameterType) {
            super.setExpectedType(parameterType);
            if (arg instanceof ActionParameter) {
                ((ActionParameter) arg).setExpectedType(String.class);
            }
        }

        Object getValue(@NotNull MatcherContext context) {
            String text;
            if (arg instanceof ActionParameter) {
                ActionParameter param = (ActionParameter) arg;
                text = (String) param.getValue(context);
            } else {
                Preconditions.checkState(arg instanceof String);
                text = (String) arg;
            }
            return text == null ? null : converter.parse(text);
        }
    }

}
