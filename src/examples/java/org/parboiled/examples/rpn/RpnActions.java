/*
 * Copyright (C) 2010 Reinier Zwitserloot, adapted by Mathias Doenitz
 */

package org.parboiled.examples.rpn;

import org.parboiled.BaseActions;
import org.parboiled.common.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RpnActions extends BaseActions<Node> {

    public Node toBigDecimal(String lastText) {
        if (StringUtils.isEmpty(lastText)) return Node.number(null);
        try {
            return Node.number(new BigDecimal(lastText));
        } catch (Exception e) {
            return Node.number(null);
        }
    }

    public Node toOperator(Character op) {
        return Node.operator(op);
    }

    public Node runStack(List<Node> values) {
        Stack<BigDecimal> stack = new Stack<BigDecimal>();
        List<String> warnings = new ArrayList<String>();
        for (int i = 0; i < values.size(); i++) {
            Node value = values.get(i);
            if (value.isNumber()) {
                stack.push(value.getNumber());
            } else if (value.isOperator()) {
                performOperation(stack, warnings, i, value);
            }
        }
        return Node.result(new ArrayList<BigDecimal>(stack), warnings);
    }

    private void performOperation(Stack<BigDecimal> stack, List<String> warnings, int index, Node value) {
        BigDecimal right = stack.isEmpty() ? null : stack.pop();
        BigDecimal left = stack.isEmpty() ? null : stack.pop();

        if (left == null && right == null) {
            warnings.add(String.format(
                    "0 operands instead of the expected 2 processing %s at %d",
                    value.getOperator(), index));
            return;
        }

        if (left == null) {
            stack.push(right);
            warnings.add(String.format(
                    "1 operand (right: %s) instead of the expected 2 processing %s at %d",
                    right, value.getOperator(), index));
            return;
        }

        if (right == null) {
            stack.push(left);
            warnings.add(String.format(
                    "1 operand (left: %s) instead of the expected 2 processing %s at %d",
                    left, value.getOperator(), index));
            return;
        }

        switch (value.getOperator()) {
            case '+':
                stack.push(left.add(right));
                break;

            case '-':
                stack.push(left.subtract(right));
                break;

            case '*':
                stack.push(left.multiply(right));
                break;

            case '/':
                stack.push(left.divide(right));
                break;

            case '^':
                try {
                    stack.push(left.pow(right.intValueExact()));
                } catch (ArithmeticException e) {
                    stack.push(BigDecimal.ONE);
                    warnings.add(String.format(
                            "Right side of power operator is %s (left: %s); expected an integer in the 32-bit range. " +
                                    "Resuming with dummy result '1' at %d", right, left, index));
                }
                break;

            default:
                stack.push(BigDecimal.ZERO);
                warnings.add(String.format("Unknown operator: %s; pushing dummy result '0' at %d",
                        value.getOperator(), index));
        }
    }
}
