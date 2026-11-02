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

    public int update(String sql, PreparedStatementSetter setter) {
        return execute(sql, preparedStatement -> {
            setter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> select(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> mapper
    ) {
        return execute(sql, preparedStatement -> {
            setter.setValues(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    private <T> T execute(String sql, StatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {
        T doInStatement(PreparedStatement preparedStatement) throws SQLException;
    }
}
