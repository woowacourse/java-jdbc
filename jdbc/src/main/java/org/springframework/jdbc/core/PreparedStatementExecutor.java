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

    public void execute(
            final PreparedStatementCreator preparedStatementCreator
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(conn)) {
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
