package com.interface21.jdbc.core;

import com.interface21.jdbc.exception.DataQueryException;
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
    private final PreparedStatementSetter preparedStatementSetter;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.preparedStatementSetter = new PreparedStatementSetter();
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
        return execute(sql, (preparedStatement -> {
            preparedStatementSetter.setValues(values, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);
            return resultSetExtractor.extractData(resultSet);
        }));
    }


    public void update(String sql, Object... values) {
        execute(sql, (preparedStatement -> {
            log.debug("query : {}", sql);
            preparedStatementSetter.setValues(values, preparedStatement);
            return preparedStatement.executeUpdate();
        }));
    }

    private <T> T execute(String sql, JdbcTemplateExecutor<T> jdbcTemplateExecutor) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            return jdbcTemplateExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataQueryException(e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }
}
