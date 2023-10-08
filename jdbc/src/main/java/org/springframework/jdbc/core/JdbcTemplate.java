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

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(sql,
                preparedStatement -> {
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    final List<T> users = new ArrayList<>();

                    while (resultSet.next()) {
                        users.add(rowMapper.rowMap(resultSet));
                    }
                    return users;
                },
                parameters
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return executeQuery(sql,
                preparedStatement -> {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return rowMapper.rowMap(resultSet);
                    }
                    throw new NoSuchElementException();
                },
                parameters
        );
    }

    public int update(final String sql, final Object... parameters) {
        return executeQuery(sql, PreparedStatement::executeUpdate, parameters);
    }

    private void setValues(
            final String sql,
            final PreparedStatement preparedStatement,
            final Object... parameters
    ) throws SQLException {
        log.debug("query : {}", sql);

        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> T executeQuery(
            final String sql,
            final CallBack<T> callBack,
            final Object... parameters
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            connection.setAutoCommit(false);
            setValues(sql, preparedStatement, parameters);
            final T result = callBack.call(preparedStatement);
            connection.commit();
            return result;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @FunctionalInterface
    private interface CallBack<T> {

        T call(PreparedStatement preparedStatement) throws SQLException;
    }
}
