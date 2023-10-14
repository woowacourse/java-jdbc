package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) throws SQLException {
        return execute(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            if (!resultSet.isLast()) {
                throw new DataAccessException("단일 데이터가 아닙니다.");
            }
            return Optional.empty();
        }, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) throws SQLException {
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

    public int update(final String sql, final Object... parameters) throws SQLException {
        return execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T execute(
            final String sql,
            final ExecuteQueryCallback<T> callBack,
            final Object... objects
    ) throws SQLException {
        final PreparedStatement preparedStatement = createPreparedStatement(sql);
        setPreparedStatement(preparedStatement, objects);

        return callBack.execute(preparedStatement);
    }

    private PreparedStatement createPreparedStatement(final String sql) throws SQLException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        return connection.prepareStatement(sql);
    }

    private void setPreparedStatement(final PreparedStatement preparedStatement, final Object[] parameters) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            preparedStatement.setObject(PARAMETER_OFFSET + parameterIndex, parameters[parameterIndex]);
        }
    }
}
