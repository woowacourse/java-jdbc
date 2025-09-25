package com.interface21.jdbc.client;

import java.util.HashMap;
import java.util.Map;

public class Insert {

    private final Sql sql;

    private final String tableName;
    private final Map<String, Object> valueMap = new HashMap<>();

    public Insert(Sql sql, String tableName) {
        this.sql = sql;
        this.tableName = tableName;
    }

    public Insert of(String key, Object value) {
        valueMap.put(key, value);
        return this;
    }

    public void execute() {
        sql.insert(this);
    }

    // 완성된 SQL의 형태를 구성한다.
    @Override
    public String toString() {
        // TODO : 구현
        return null;
    }
}
