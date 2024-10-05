package com.interface21.jdbc.querybuilder.query;

import com.interface21.jdbc.querybuilder.ConditionExpression;
import com.interface21.jdbc.querybuilder.QueryMethod;
import java.util.List;
import java.util.StringJoiner;

public class SelectQuery extends Query {

    private static final QueryMethod method = QueryMethod.SELECT;

    private final String sql;

    public static SelectQuery from(List<String> projections, String table, List<ConditionExpression> conditions) {
        StringJoiner queryJoiner = getQueryJoiner();
        resolveQueryHeader(projections, table, queryJoiner);
        resoleQueryCondition(conditions, queryJoiner);
        return new SelectQuery(queryJoiner.toString());
    }

    private static void resoleQueryCondition(List<ConditionExpression> conditions, StringJoiner queryJoiner) {
        if (!conditions.isEmpty()) {
            queryJoiner.add("where");
            conditions.forEach(condition -> queryJoiner.add(condition.getExpression()));
        }
    }

    private static void resolveQueryHeader(List<String> projections, String table, StringJoiner queryJoiner) {
        StringJoiner fieldJoiner = getFieldJoiner();
        queryJoiner.add(method.getSql());
        projections.forEach(fieldJoiner::add);
        queryJoiner.add(fieldJoiner.toString());
        queryJoiner.add("from " + table);
    }

    private SelectQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
