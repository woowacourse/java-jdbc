package com.interface21.jdbc.querybuilder;

import com.interface21.jdbc.querybuilder.query.Query;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    private QueryMethod method;
    private List<String> fieldNames;
    private String table;
    private List<ConditionExpression> conditions;

    public QueryBuilder() {
        this.conditions = new ArrayList<>();
        this.fieldNames = new ArrayList<>();
    }

    public QueryBuilder from(String tableName) {
        this.table = tableName;
        return this;
    }

    public QueryBuilder condition(List<ConditionExpression> condition) {
        conditions.addAll(condition);
        return this;
    }

    public QueryBuilder where(ConditionExpression condition) {
        return condition(List.of(condition));
    }

    public QueryBuilder select(List<String> fieldNames) {
        this.method = QueryMethod.SELECT;
        this.fieldNames.addAll(fieldNames);
        return this;
    }

    public QueryBuilder selectFrom(String tableName) {
        select(List.of("*"));
        from(tableName);
        return this;
    }

    public QueryBuilder insert(List<String> fieldNames) {
        this.method = QueryMethod.INSERT;
        this.fieldNames.addAll(fieldNames);
        return this;
    }

    public QueryBuilder update(List<String> fieldNames) {
        this.method = QueryMethod.UPDATE;
        this.fieldNames.addAll(fieldNames);
        return this;
    }

    public Query build() {
        return Query.from(method, table, fieldNames, conditions);
    }
}
