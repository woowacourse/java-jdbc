package com.interface21.jdbc.querybuilder.query;

import com.interface21.jdbc.querybuilder.ConditionExpression;
import com.interface21.jdbc.querybuilder.QueryMethod;
import java.util.List;
import java.util.StringJoiner;

public class UpdateQuery extends Query {

    private static final QueryMethod method = QueryMethod.UPDATE;

    private final String sql;

    private UpdateQuery(String sql) {
        this.sql = sql;
    }

    public static UpdateQuery from(List<String> updateFields, String table, List<ConditionExpression> conditions) {
        StringJoiner queryJoiner = new StringJoiner(" ");
        resolveQueryHeader(table, queryJoiner);
        resolveQueryFields(updateFields, queryJoiner);
        resolveQueryConditions(conditions, queryJoiner);
        return new UpdateQuery(queryJoiner.toString());
    }

    private static void resolveQueryConditions(List<ConditionExpression> conditions, StringJoiner queryJoiner) {
        if (!conditions.isEmpty()) {
            queryJoiner.add("where");
            conditions.forEach(condition -> queryJoiner.add(condition.getExpression()));
        }
    }

    private static void resolveQueryFields(List<String> updateFields, StringJoiner queryJoiner) {
        StringJoiner fieldJoiner = getFieldJoiner();
        updateFields.forEach(fieldName -> fieldJoiner.add(fieldName + " = " + "?"));
        queryJoiner.add(fieldJoiner.toString());
    }

    private static void resolveQueryHeader(String table, StringJoiner queryJoiner) {
        queryJoiner.add(method.getSql());
        queryJoiner.add(table);
        queryJoiner.add("set");
    }

    @Override
    public String getSql() {
        return sql;
    }
}
