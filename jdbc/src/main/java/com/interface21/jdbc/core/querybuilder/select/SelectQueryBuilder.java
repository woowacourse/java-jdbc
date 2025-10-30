package com.interface21.jdbc.core.querybuilder.select;

import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SelectQueryBuilder<T> implements SelectFromStep<T>, SelectColumnsStep<T>, SelectWhereStep<T> {

    private final JdbcTemplate jdbcTemplate;
    private final Class<T> mappedClass;
    private String tableName;
    private List<String> selectedColumns = new ArrayList<>();
    private final List<String> whereColumns = new ArrayList<>();
    private final List<Object> whereParams = new ArrayList<>();

    public SelectQueryBuilder(JdbcTemplate jdbcTemplate, Class<T> mappedClass) {
        this.jdbcTemplate = jdbcTemplate;
        this.mappedClass = mappedClass;
    }

    @Override
    public SelectWhereStep<T> from(String tableName) {
        this.tableName = tableName;
        return this;
    }

    @Override
    public SelectFromStep<T> columns(String... columns) {
        this.selectedColumns.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public SelectFromStep<T> allColumns() {
        this.selectedColumns.add("*"); // '*'로 모든 컬럼 표시
        return this;
    }

    @Override
    public SelectWhereStep<T> where(String column, Object value) {
        this.whereColumns.add(column + " = ?");
        this.whereParams.add(value);
        return this;
    }

    @Override
    public List<T> toList(Connection connection) {
        String sql = buildSelectSql();
        return jdbcTemplate.query(connection, sql, mappedClass, whereParams.toArray());
    }

    @Override
    public Optional<T> findFirst(Connection connection) {
        String sql = buildSelectSql();
        return jdbcTemplate.queryForObject(connection, sql, mappedClass, whereParams.toArray());
    }

    private String buildSelectSql() {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalStateException("FROM clause is required.");
        }
        if (selectedColumns.isEmpty()) {
            throw new IllegalStateException("At least one column or allColumns() must be specified.");
        }

        String columnsToSelect = String.join(", ", selectedColumns);
        StringBuilder sqlBuilder = new StringBuilder("SELECT ")
                .append(columnsToSelect)
                .append(" FROM ")
                .append(tableName);

        if (!whereColumns.isEmpty()) {
            sqlBuilder.append(" WHERE ")
                      .append(String.join(" AND ", whereColumns));
        }
        return sqlBuilder.toString();
    }
}
