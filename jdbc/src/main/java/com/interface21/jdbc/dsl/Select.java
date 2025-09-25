package com.interface21.jdbc.dsl;

import com.interface21.jdbc.core.ResultExtractor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Select {

    private final DslJdbcTemplate dslJdbcTemplate;

    private String tableName;
    private final List<String> rowList;
    private final Map<String, Object> whereMap = new HashMap<>();

    public Select(DslJdbcTemplate dslJdbcTemplate, String... rowNames) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.rowList = Arrays.stream(rowNames).toList();
    }

    public Select from(String table) {
        this.tableName = table;
        return this;
    }

    public Select where(String key, Object value) {
        this.whereMap.put(key, value);
        return this;
    }

    // TODO : ResultExtractor를 구현하지 않아도 추출 가능하도록 팩토리 사용
    public <T> List<T> many(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectMany(this, extractor);
    }

    public <T> T one(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectOne(this, extractor);
    }

    /*
    select ... from %s
    where ...
     */

    // 완성된 SQL의 형태를 구성한다.
    @Override
    public String toString() {
        String rows = rowList.stream().reduce((a, b) -> a + ", " + b).orElse("");
        String wheres = whereMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + " AND " + b)
                .orElse("");
        return "SELECT %s FROM %s WHERE %s".formatted(rows, tableName, wheres);
    }
}
