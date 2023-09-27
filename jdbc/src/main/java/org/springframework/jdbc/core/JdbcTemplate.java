package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        log.debug("query : {}", sql);
        execute(sql, new PreparedStatementExecutor<>() {
            @Override
            public Object fetchData(ResultSet resultSet) throws SQLException {
                return null;
            }

            @Override
            public ResultSet fetchResultSet(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.execute();
                return null;
            }
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("query : {}", sql);

        return execute(sql, new PreparedStatementExecutor<T>() {
            @Override
            public T fetchData(ResultSet resultSet) throws SQLException {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
                return null;
            }

            @Override
            public ResultSet fetchResultSet(PreparedStatement preparedStatement) throws SQLException {
                return preparedStatement.executeQuery();
            }
        }, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("query : {}", sql);
        return execute(sql, new PreparedStatementExecutor<>() {
            @Override
            public List<T> fetchData(ResultSet resultSet) throws SQLException {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }

            @Override
            public ResultSet fetchResultSet(PreparedStatement preparedStatement) throws SQLException {
                return preparedStatement.executeQuery();
            }
        }, args);
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args);
                ResultSet rs = preparedStatementExecutor.fetchResultSet(preparedStatement);
        ) {
            return preparedStatementExecutor.fetchData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection, Object... args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setValues(preparedStatement, args);
        return preparedStatement;
    }

    private void setValues(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
