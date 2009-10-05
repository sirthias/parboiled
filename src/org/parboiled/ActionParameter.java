package org.parboiled;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import static org.parboiled.support.ParseTreeUtils.collectNodesByPath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

abstract class ActionParameter {

    protected final String path;
    protected Class<?> expectedParameterType;

    ActionParameter(String path) {
        this.path = path;
    }

    public void setExpectedType(Class<?> parameterType) {
        expectedParameterType = parameterType;
    }

    abstract Object getValue(@NotNull MatcherContext context);

    static class Node extends ActionParameter {
        Node(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Checks.ensure(expectedParameterType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getCurrentPath(), expectedParameterType, org.parboiled.Node.class);
            Preconditions.checkState(expectedParameterType.isAssignableFrom(org.parboiled.Node.class));
            return context.getNodeByPath(path);
        }
    }

    static class Nodes extends ActionParameter {
        public Nodes(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Checks.ensure(expectedParameterType.isArray() && componentType.isAssignableFrom(org.parboiled.Node.class),
                    "Illegal action argument in '%s', expected %s instead of %s",
                    context.getCurrentPath(), expectedParameterType, org.parboiled.Node[].class);
            ArrayList<org.parboiled.Node> list = collectNodesByPath(context.getCurrentNodes(), path,
                    new ArrayList<org.parboiled.Node>());
            return list.toArray((org.parboiled.Node[]) Array.newInstance(componentType, list.size()));
        }
    }

    static class Value extends ActionParameter {
        public Value(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            org.parboiled.Node node = context.getNodeByPath(path);
            if (node == null || node.getValue() == null) return null;
            Object value = node.getValue();
            Checks.ensure(expectedParameterType.isAssignableFrom(value.getClass()),
                    "Illegal action argument in '%s', cannot cast %s to %s",
                    context.getCurrentPath(), value.getClass(), expectedParameterType);
            return value;
        }
    }

    static class Values extends ActionParameter {
        public Values(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Class<?> componentType = expectedParameterType.getComponentType();
            Preconditions.checkState(expectedParameterType.isArray() && !componentType.isPrimitive());
            List<org.parboiled.Node> nodes = collectNodesByPath(context.getCurrentNodes(), path,
                    new ArrayList<org.parboiled.Node>());
            Object array = Array.newInstance(componentType, nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                Object value = nodes.get(i).getValue();
                if (value == null) continue;
                Checks.ensure(componentType.isAssignableFrom(value.getClass()),
                        "Illegal action argument in '%s', cannot cast value array component from %s to %s",
                        context.getCurrentPath(), value.getClass(), componentType);
                Array.set(array, i, value);
            }
            return array;
        }
    }

    static class Text extends ActionParameter {
        public Text(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(String.class));
            org.parboiled.Node node = context.getNodeByPath(path);
            return context.getNodeText(node);
        }
    }

    static class Texts extends ActionParameter {
        public Texts(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == String.class);
            List<org.parboiled.Node> nodes = collectNodesByPath(context.getCurrentNodes(), path,
                    new ArrayList<org.parboiled.Node>());
            String[] texts = new String[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                texts[i] = context.getNodeText(nodes.get(i));
            }
            return texts;
        }
    }

    static class Char extends ActionParameter {
        public Char(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(Character.class));
            org.parboiled.Node node = context.getNodeByPath(path);
            return context.getNodeChar(node);
        }
    }

    static class Chars extends ActionParameter {
        public Chars(String path) {
            super(path);
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(
                    expectedParameterType.isArray() && expectedParameterType.getComponentType() == Character.class);
            List<org.parboiled.Node> nodes = collectNodesByPath(context.getCurrentNodes(), path,
                    new ArrayList<org.parboiled.Node>());
            Character[] chars = new Character[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                chars[i] = context.getNodeChar(nodes.get(i));
            }
            return chars;
        }
    }

    static class FirstOfNonNull extends ActionParameter {
        private final Object[] args;

        public FirstOfNonNull(Object[] args) {
            super(null);
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
                            context.getCurrentPath(), arg.getClass(), expectedParameterType);
                    return arg;
                }
            }
            return null;
        }
    }

    static class ConvertToInteger extends ActionParameter {
        private final Object arg;

        public ConvertToInteger(Object arg) {
            super(null);
            this.arg = arg;
        }

        @Override
        public void setExpectedType(Class<?> parameterType) {
            super.setExpectedType(parameterType);
            if (arg instanceof ActionParameter) {
                ((ActionParameter) arg).setExpectedType(String.class);
            }
        }

        Object getValue(@NotNull MatcherContext context) {
            Preconditions.checkState(expectedParameterType.isAssignableFrom(Integer.class));
            String text;
            if (arg instanceof ActionParameter) {
                ActionParameter param = (ActionParameter) arg;
                text = (String) param.getValue(context);
            } else {
                Preconditions.checkState(arg instanceof String);
                text = (String) arg;
            }
            return text != null ? Integer.valueOf(text) : null;
        }
    }
}
