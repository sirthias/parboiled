/*
 * Copyright (C) 2010 Reinier Zwitserloot, adapted by Mathias Doenitz
 */

package org.parboiled.examples.rpn;

import org.parboiled.common.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class Node {
    private final BigDecimal number;
    private final char operator;
    private final List<BigDecimal> result;
    private final List<String> warnings;

    private Node(BigDecimal number, char operator, List<BigDecimal> result, List<String> warnings) {
        this.number = number;
        this.operator = operator;
        this.result = result;
        this.warnings = warnings;
    }

    public boolean isOperator() {
        return operator != '\0';
    }

    public boolean isNumber() {
        return result == null && operator == '\0';
    }

    public BigDecimal getNumber() {
        return number;
    }

    public char getOperator() {
        return operator;
    }

    public String getWarnings() {
        if (warnings.isEmpty()) return "";
        if (warnings.size() == 1) return warnings.get(0);
        return StringUtils.join(warnings, "\n---\n");
    }

    public List<BigDecimal> getResult() {
        return result;
    }

    @Override
    public String toString() {
        if (isOperator()) return String.valueOf(operator);
        if (isNumber()) return number == null ? "NaN" : number.toString();
        return result.toString();
    }

    public static Node number(BigDecimal number) {
        return new Node(number, '\0', null, null);
    }

    public static Node operator(Character op) {
        return new Node(null, op == null ? '?' : op, null, null);
    }

    public static Node result(List<BigDecimal> result, List<String> warnings) {
        return new Node(null, '\0',
                result == null ? Collections.<BigDecimal>emptyList() : result,
                warnings == null ? Collections.<String>emptyList() : warnings
        );
    }

}
