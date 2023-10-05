package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.DataAccessException;

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

    public int update(String sql, Object... arguments) {
        return execute(sql, PreparedStatement::executeUpdate,
                arguments
        );
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        return execute(sql, preparedStatement -> {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) {
                        throw new DataAccessException("결과가 없습니다");
                    }

                    return rowMapper.map(resultSet);
                },
                arguments
        );
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... arguments) {
        return execute(sql, preparedStatement -> {
                    ResultSet resultSet = preparedStatement.executeQuery();

                    List<T> results = new ArrayList<>();
                    while (resultSet.next()) {
                        results.add(rowMapper.map(resultSet));
                    }
                    return results;
                },
                arguments
        );
    }

    private <V> V execute(String sql, PreparedStatementExecutor<V> preparedStatementExecutor, Object... arguments) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArguments(preparedStatement, arguments);

            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setArguments(PreparedStatement preparedStatement, Object[] arguments) throws SQLException {
        for (int i = 0; i < arguments.length; i++) {
            preparedStatement.setObject(i + 1, arguments[i]);
        }
    }
}
