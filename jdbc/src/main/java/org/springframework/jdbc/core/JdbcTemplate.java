package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final ConnectionTemplate connectionTemplate;

    public JdbcTemplate() {
        this.connectionTemplate = new ConnectionTemplate();
    }

    public <T> List<T> query(final Connection connection,
                             final String sql,
                             final RowMapper<T> rowMapper,
                             final Object... arguments) {
        return connectionTemplate.readResult(connection, sql, resultSet -> {
            final List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet));
            }
            return list;
        }, arguments);
    }

    public <T> Optional<T> querySingleRow(final Connection connection,
                                          final String sql,
                                          final RowMapper<T> rowMapper,
                                          final Object... arguments) {
        return connectionTemplate.readResult(connection, sql, resultSet -> {
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        }, arguments);
    }

    public void update(final Connection connection,
                       final String sql,
                       final Object... arguments) {
        connectionTemplate.update(connection, sql, PreparedStatement::executeUpdate, arguments);
    }

    private static class ConnectionTemplate {

        private final Logger log = LoggerFactory.getLogger(ConnectionTemplate.class);

        public ConnectionTemplate() {}

        public <T> T readResult(final Connection connection,
                                final String sql,
                                final SelectQueryExecutor<T> selectQueryExecutor,
                                final Object... parameters) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                setQueryParameter(preparedStatement, parameters);
                final ResultSet resultSet = preparedStatement.executeQuery();

                return selectQueryExecutor.execute(resultSet);
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }

        public void update(final Connection connection,
                           final String sql,
                           final UpdateQueryExecutor updateQueryExecutor,
                           final Object... parameters) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                setQueryParameter(preparedStatement, parameters);

                updateQueryExecutor.execute(preparedStatement);
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }

        private void setQueryParameter(final PreparedStatement preparedStatement,
                                       final Object[] parameters) throws SQLException {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        }
    }
}
