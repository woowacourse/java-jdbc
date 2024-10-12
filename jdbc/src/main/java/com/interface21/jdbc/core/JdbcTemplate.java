package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final QueryExecutor queryExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryExecutor = new QueryExecutor(dataSource);
    }

    public <T> List<T> queryForList(RowMapper<T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            List<T> result = new LinkedList<>();
            while (rs.next()) {
                result.add(mapper.mapRow(rs));
            }
            return result;
        }, sql, args);
    }

    public <T> T queryForObject(RowMapper<T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return mapper.mapRow(rs);
            }
            return null;
        }, sql, args);
    }

    public void command(String sql, Object... args) {
        queryExecutor.executeConsumer(PreparedStatement::execute, sql, args);
    }
}
