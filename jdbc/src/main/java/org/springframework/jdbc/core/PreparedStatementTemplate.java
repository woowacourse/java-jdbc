package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.JdbcTemplateException;

public class PreparedStatementTemplate {

    private final DataSource dataSource;

    public PreparedStatementTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementCreator creator,
            final PreparedStatementExecutor<T> executor
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = creator.create(connection)
        ) {
            return executor.execute(preparedStatement);
        } catch (final SQLException e) {
            throw new JdbcTemplateException(e);
        }
    }
}
