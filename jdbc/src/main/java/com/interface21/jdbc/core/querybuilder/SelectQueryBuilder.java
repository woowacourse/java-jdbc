// com/interface21/jdbc/core/querybuilder/SelectQueryBuilder.java

package com.interface21.jdbc.core.querybuilder;

import com.interface21.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SelectQueryBuilder<T> {

    private final JdbcTemplate jdbcTemplate;
    private final Class<T> mappedClass;
    private String sql;
    private final List<Object> params = new ArrayList<>();

    public SelectQueryBuilder(JdbcTemplate jdbcTemplate, Class<T> mappedClass) {
        this.jdbcTemplate = jdbcTemplate;
        this.mappedClass = mappedClass;
    }

    /**
     * 실행할 SQL 쿼리를 설정합니다.
     */
    public SelectQueryBuilder<T> sql(String sql) {
        this.sql = sql;
        return this;
    }

    /**
     * SQL의 ?에 순서대로 바인딩될 파라미터를 추가합니다.
     */
    public SelectQueryBuilder<T> param(Object param) {
        this.params.add(param);
        return this;
    }

    /**
     * 쿼리를 실행하고 결과를 List<T> 형태로 반환합니다.
     */
    public List<T> toList() {
        if (sql == null || sql.isBlank()) {
            throw new IllegalStateException("SQL must be provided before executing a query.");
        }
        return jdbcTemplate.query(sql, mappedClass, params.toArray());
    }

    /**
     * 쿼리를 실행하고 결과를 Optional<T> 형태로 반환합니다.
     */
    public Optional<T> findFirst() {
        if (sql == null || sql.isBlank()) {
            throw new IllegalStateException("SQL must be provided before executing a query.");
        }
        return jdbcTemplate.queryForObject(sql, mappedClass, params.toArray());
    }
}
