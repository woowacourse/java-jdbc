package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.JdbcException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = setValues(sql, connection, parameters);
                final ResultSet resultSet = executeQuery(PreparedStatement::executeQuery, preparedStatement)
        ) {
            final List<T> users = new ArrayList<>();

            while (resultSet.next()) {
                T result = rowMapper.rowMap(resultSet);
                users.add(result);
            }
            return users;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public final <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = setValues(sql, connection, parameters);
                final ResultSet resultSet = executeQuery(PreparedStatement::executeQuery, preparedStatement)
        ) {
            if (resultSet.next()) {
                return rowMapper.rowMap(resultSet);
            }

            throw new NoSuchElementException();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public final int update(final String sql, final Object... parameters) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = setValues(sql, connection, parameters)
        ) {
            return executeQuery(PreparedStatement::executeUpdate, preparedStatement);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    private PreparedStatement setValues(
            final String sql,
            final Connection connection,
            final Object... parameters
    ) throws SQLException {
        log.debug("query : {}", sql);

        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
        return preparedStatement;
    }

    private <T> T executeQuery(
            final CallBack<T> callBack,
            final PreparedStatement preparedStatement
    ) throws SQLException {
        return callBack.call(preparedStatement);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @FunctionalInterface
    private interface CallBack<T> {

        T call(PreparedStatement preparedStatement) throws SQLException;
    }
}
