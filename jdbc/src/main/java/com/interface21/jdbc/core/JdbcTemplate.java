package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object ... parameters) {
        return execute(sql, preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> result = new ArrayList<>();
                int rowNum = 0;

                while (resultSet.next()) {
                    result.add(rowMapper.map(resultSet, rowNum++));
                }

                return result;
            }
        }, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object ... parameters) {
        List<T> results = query(sql, rowMapper, parameters);

        if (results.size() != 1) {
            throw new DataAccessException("결과가 1건이어야 하지만 " + results.size() + "건입니다.");
        }

        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback, final Object... parameters) {
        try (Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }

            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다", e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("JDBC 커넥션을 얻는 데 실패했습니다.", e);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
    }
}
