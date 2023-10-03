package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionTemplate {

    private final Logger log = LoggerFactory.getLogger(ConnectionTemplate.class);

    private final DataSource dataSource;

    public ConnectionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(final String sql,
                       final PreparedStatementExecutor<T> preparedStatementExecutor,
                       final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setQueryParameter(preparedStatement, parameters);

            return preparedStatementExecutor.execute(preparedStatement);
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
