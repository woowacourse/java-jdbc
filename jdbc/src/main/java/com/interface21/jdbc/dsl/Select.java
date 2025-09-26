package com.interface21.jdbc.dsl;

import com.interface21.jdbc.core.ResultExtractor;
import java.util.List;

public class Select {

    private final DslJdbcTemplate dslJdbcTemplate;

    private String tableName;
    private final ColumnNames columnNames = new ColumnNames();
    private final Where where = new Where();

    public Select(DslJdbcTemplate dslJdbcTemplate, String... rowNames) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.columnNames.add(rowNames);
    }

    public Select from(String table) {
        this.tableName = table;
        return this;
    }

    public Select where(String key, Object value) {
        this.where.add(key, value);
        return this;
    }

    // TODO : ResultExtractor를 구현하지 않아도 추출 가능하도록 팩토리 사용
    public <T> List<T> many(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectMany(this, extractor);
    }

    public <T> T one(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectOne(this, extractor);
    }

    @Override
    public String toString() {
        return "SELECT %s FROM %s %s".formatted(columnNames, tableName, where);
    }
}
