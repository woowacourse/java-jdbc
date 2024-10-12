package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate() {
    }

    public int executeUpdate(Connection connection, String sql, Object... parameters) {
        return execute(connection, sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> List<T> queryForList(Connection connection, String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(connection, sql, statement -> {
            ResultSet resultSet = statement.executeQuery();
            List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }

            return result;
        }, parameters);
    }

    private <T> T execute(Connection connection, String sql, StatementExecutor<T> executor, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            return executor.apply(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }
}
