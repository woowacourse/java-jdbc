package com.interface21.jdbc.querybuilder.query;

import com.interface21.jdbc.querybuilder.QueryMethod;
import java.util.List;
import java.util.StringJoiner;

public class InsertQuery extends Query {

    private static final QueryMethod method = QueryMethod.INSERT;

    private final String sql;

    public static InsertQuery from(List<String> insertFields, String table) {
        StringJoiner queryJoiner = getQueryJoiner();
        resolveQueryHeader(insertFields, table, queryJoiner);
        resolveInsertFields(insertFields, queryJoiner);
        return new InsertQuery(queryJoiner.toString());
    }

    private static void resolveInsertFields(List<String> insertFields, StringJoiner queryJoiner) {
        StringJoiner insertFieldsJoiner = getInsertFieldJoiner();
        for (int i = 0; i < insertFields.size(); i++) {
            insertFieldsJoiner.add("?");
        }
        queryJoiner.add(insertFieldsJoiner.toString());
    }

    private static void resolveQueryHeader(List<String> insertFields, String table, StringJoiner queryJoiner) {
        StringJoiner insertFieldsJoiner = getInsertFieldJoiner();
        queryJoiner.add(method.getSql());
        queryJoiner.add(table);
        insertFields.forEach(insertFieldsJoiner::add);
        queryJoiner.add(insertFieldsJoiner.toString());
        queryJoiner.add("values");
    }

    private InsertQuery(String sql) {
        this.sql = sql;
    }

    private static StringJoiner getInsertFieldJoiner() {
        return new StringJoiner(",", "(", ")");
    }

    @Override
    public String getSql() {
        return sql;
    }
}
