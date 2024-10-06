package com.interface21.jdbc.querybuilder;

public enum QueryMethod {

    SELECT("select"),
    INSERT("insert into"),
    UPDATE("update"),
    ;

    private final String sql;

    QueryMethod(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
