package com.interface21.jdbc.querybuilder;

import com.interface21.jdbc.querybuilder.query.Query;
import java.util.ArrayList;
import java.util.Arrays;
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

    public QueryBuilder into(String tableName) {
        this.table = tableName;
        return this;
    }

    public QueryBuilder update(String tableName) {
        this.table = tableName;
        return this;
    }

    public QueryBuilder where(ConditionExpression condition, ConditionExpression ... conditions) {
        this.conditions.add(condition);
        this.conditions.addAll(Arrays.asList(conditions));
        return this;
    }

    public QueryBuilder select(String firstField, String...fieldNames) {
        this.method = QueryMethod.SELECT;
        this.fieldNames.add(firstField);
        this.fieldNames.addAll(Arrays.asList(fieldNames));
        return this;
    }

    public QueryBuilder selectFrom(String tableName) {
        select("*");
        from(tableName);
        return this;
    }

    public QueryBuilder insert(String firstField, String...fieldNames) {
        this.method = QueryMethod.INSERT;
        this.fieldNames.add(firstField);
        this.fieldNames.addAll(Arrays.asList(fieldNames));
        return this;
    }

    public QueryBuilder set(String firstField, String... fieldNames) {
        this.method = QueryMethod.UPDATE;
        this.fieldNames.add(firstField);
        this.fieldNames.addAll(Arrays.asList(fieldNames));
        return this;
    }

    public Query build() {
        return Query.from(method, table, fieldNames, conditions);
    }
}
