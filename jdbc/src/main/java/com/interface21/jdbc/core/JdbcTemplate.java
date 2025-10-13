package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        executeQuery(
                sql, preparedStatementSetter,
                (preparedStatement) -> preparedStatement.executeUpdate()
        );
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return executeQuery(
                sql, preparedStatementSetter,
                (preparedStatement) -> {
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        if (rs.next()) {
                            return rowMapper.mapped(rs);
                        }
                    }
                    return null;
                }
        );
    }

    public <T> List<T> queryAll(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return executeQuery(
                sql, preparedStatementSetter,
                (preparedStatement) -> {
                    List<T> results = new ArrayList<>();
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            T result = rowMapper.mapped(rs);
                            results.add(result);
                        }
                    }

                    return results;
                }
        );
    }

    private <T> T executeQuery(String sql, PreparedStatementSetter preparedStatementSetter,
                               QueryExecution<T> queryExecution) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatementSetter.setValues(preparedStatement);
            return queryExecution.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    private interface QueryExecution<T> {
        T execute(PreparedStatement preparedStatement) throws SQLException;
    }
}
