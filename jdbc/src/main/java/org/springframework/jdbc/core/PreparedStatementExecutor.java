package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class PreparedStatementExecutor {

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementCreator preparedStatementCreator,
            final PreparedStatementCaller<T> preparedStatementCaller
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(conn)) {
            return preparedStatementCaller.execute(preparedStatement);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
