package com.interface21.jdbc.querybuilder;

import java.util.StringJoiner;

public class ConditionExpression {

    private final StringJoiner joiner = new StringJoiner(" ");
    private final String fieldName;
    private final String operator;

    public ConditionExpression(String fieldName, String operator) {
        this.fieldName = fieldName;
        this.operator = operator;
    }

    public static ConditionExpression eq(String fieldName) {
        return new ConditionExpression(fieldName, "=");
    }

    public String getExpression() {
        joiner.add(fieldName);
        joiner.add(operator);
        joiner.add("?");
        return joiner.toString();
    }

    public String getOperator() {
        return operator;
    }
}
