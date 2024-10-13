package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final QueryExecutor queryExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryExecutor = new QueryExecutor(dataSource);
    }

    public <T> List<T> query(Connection connection, RowMapper<ResultSet, T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(connection, preparedStatement -> {
            List<T> result = new LinkedList<>();
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                result.add(mapper.mapRow(rs));
            }
            return result;
        }, sql, args);
    }

    public <T> T queryForObject(Connection connection, RowMapper<ResultSet, T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(connection, preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            return mapper.mapRow(rs);
        }, sql, args);
    }

    public void command(Connection connection, String sql, Object... args) {
        queryExecutor.executeConsumer(connection, PreparedStatement::execute, sql, args);
    }
}
