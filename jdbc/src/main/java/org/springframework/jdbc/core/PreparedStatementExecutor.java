package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class PreparedStatementExecutor {

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementCreator preparedStatementCreator,
            final PreparedStatementCaller<T> preparedStatementCaller
    ) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(conn)) {
            return preparedStatementCaller.call(preparedStatement);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
