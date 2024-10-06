package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        return Optional.ofNullable(executeQuery(sql, resultSet -> {
                    if (resultSet.next()) {
                        return rowMapper.mapRow(resultSet);
                    }
                    return null;
                }, values)
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        return executeQuery(sql, resultSet -> {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        }, values);
    }

    private <T> T executeQuery(String sql, ResultSetExtractor<T> resultSetExtractor, Object... values) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            assignSqlValues(values, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);
            return resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(String sql, Object... values) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            log.debug("query : {}", sql);

            assignSqlValues(values, preparedStatement);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }

    private void assignSqlValues(Object[] values, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= values.length; i++) {
            preparedStatement.setObject(i, values[i - 1]);
        }
    }
}
