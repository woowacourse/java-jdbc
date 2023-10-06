package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementAgent {

    private static final Logger log = LoggerFactory.getLogger(StatementAgent.class);

    private final DataSource dataSource;

    public StatementAgent(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T service(final Connection connection, final String sql, final StatementCallback<T> statementCallback,
                         final Object... args) {
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, args);
            return statementCallback.call(statement);

        } catch (SQLException e) {
            log.error("statement agent service failed.");
            throw new DataAccessException(e);
        }
    }

    public <T> T service(final String sql, final StatementCallback<T> statementCallback, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, args);
            return statementCallback.call(statement);

        } catch (SQLException e) {
            log.error("statement agent service failed.");
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
