package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryAgent {

    private static final Logger log = LoggerFactory.getLogger(QueryAgent.class);

    private final DataSource dataSource;

    public QueryAgent(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T service(final String sql, final QueryCallback<T> queryCallback, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, args);
            return queryCallback.call(statement);

        } catch (SQLException e) {
            log.error("query agent service failed.");
            throw new QueryAgentServiceFailedException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
