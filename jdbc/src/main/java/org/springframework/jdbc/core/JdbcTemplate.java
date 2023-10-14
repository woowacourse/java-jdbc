package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private static final int PARAMETER_OFFSET = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            if (!resultSet.isLast()) {
                throw new RuntimeException("단일 데이터가 아닙니다.");
            }
            return Optional.empty();
        }, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                final T object = rowMapper.mapRow(resultSet);
                objects.add(object);
            }
            return objects;
        });
    }

    public int update(final String sql, final Object... parameters) {
        return execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T execute(final String sql, final ExecuteQueryCallback<T> callBack, final Object... objects) {
        try {
            final PreparedStatement preparedStatement = createPreparedStatement(sql);
            setPreparedStatement(preparedStatement, objects);

            final T result = callBack.execute(preparedStatement);
            TransactionSynchronizationManager.commitTransaction(dataSource);

            return result;
        } catch (final SQLException ex) {
            throw new RuntimeException("실행 중 예외가 발생했습니다.");
        }
    }

    private PreparedStatement createPreparedStatement(final String sql) {
        try {
            final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
            return connection.prepareStatement(sql);
        } catch (SQLException ex) {
            TransactionSynchronizationManager.rollback(dataSource);

            log.error(ex.getMessage());
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }

    private void setPreparedStatement(final PreparedStatement preparedStatement, final Object[] parameters) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            preparedStatement.setObject(PARAMETER_OFFSET + parameterIndex, parameters[parameterIndex]);
        }
    }
}
