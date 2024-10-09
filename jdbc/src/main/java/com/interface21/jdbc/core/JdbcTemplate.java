package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;
    private final QueryExecutor queryExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.queryExecutor = new QueryExecutor(dataSource);
    }

    public <T> List<T> queryForList(Function<ResultSet, T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            List<T> result = new LinkedList<>();
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
            return result;
        }, sql, args);
    }

    public <T> T queryForObject(Function<ResultSet, T> mapper, String sql, Object... args) {
        return queryExecutor.executeFunction(preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return mapper.apply(rs);
            }
            return null;
        }, sql, args);
    }

    public void command(String sql, Object... args) {
        queryExecutor.executeConsumer(PreparedStatement::execute, sql, args);
    }
}
