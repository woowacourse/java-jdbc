package org.springframework.jdbc.core;

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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        return execute(sql, preparedStatement -> mapTable(preparedStatement, rowMapper), values);
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(connection, sql, values)
        ) {
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStatement(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        return preparedStatement;
    }

    private <T> List<T> mapTable(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);
            results.add(result);
        }
        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        return execute(sql, preparedStatement -> mapRow(preparedStatement, rowMapper), values);
    }

    private <T> T mapRow(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet);
        }
        return null;
    }

    public void update(String sql, Object... values) {
        execute(sql, PreparedStatement::executeUpdate, values);
    }
}
