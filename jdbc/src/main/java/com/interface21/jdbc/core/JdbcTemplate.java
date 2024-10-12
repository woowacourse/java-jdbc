package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final QueryExecutor queryExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryExecutor = new QueryExecutor(dataSource);
    }

    public <T> T query(RowMapper<T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            return mapper.mapRow(rs);
        }, sql, args);
    }

    public void command(String sql, Object... args) {
        queryExecutor.executeConsumer(PreparedStatement::execute, sql, args);
    }
}
