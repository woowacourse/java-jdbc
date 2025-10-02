package com.interface21.jdbc.dsl.statement;

import com.interface21.jdbc.core.ResultExtractor;
import com.interface21.jdbc.dsl.DslJdbcTemplate;
import com.interface21.jdbc.dsl.ResultExtractorFactory;
import com.interface21.jdbc.dsl.component.ColumnNames;
import com.interface21.jdbc.dsl.component.Where;
import java.util.List;
import java.util.Map;

public class Select {

    private final DslJdbcTemplate dslJdbcTemplate;

    private String tableName;
    private final ColumnNames columnNames = new ColumnNames();
    private final Where where = new Where();

    public Select(DslJdbcTemplate dslJdbcTemplate, String... colNames) {
        this.dslJdbcTemplate = dslJdbcTemplate;
        this.columnNames.add(colNames);
    }

    public Select from(String table) {
        this.tableName = table;
        return this;
    }

    public Select where(Map<String, Object> map) {
        map.forEach(where::add);
        return this;
    }

    public Select where(String key, Object value) {
        this.where.add(key, value);
        return this;
    }

    public <T> List<T> many(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectMany(this, extractor);
    }

    public <T> List<T> many(Class<T> clazz) {
        ResultExtractor<T> extractor = ResultExtractorFactory.create(clazz);
        return many(extractor);
    }

    public <T> T one(ResultExtractor<T> extractor) {
        return dslJdbcTemplate.selectOne(this, extractor);
    }

    public <T> T one(Class<T> clazz) {
        ResultExtractor<T> extractor = ResultExtractorFactory.create(clazz);
        return one(extractor);
    }

    @Override
    public String toString() {
        return "SELECT %s FROM %s %s".formatted(columnNames, tableName, where);
    }
}
