package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.PreparedStatementTemplateException;

public class PreparedStatementTemplate {

    private static final int START_STATEMENT_INDEX = 1;

    private final DataSource dataSource;

    public PreparedStatementTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementCreator creator,
            final PreparedStatementExecutor<T> executor,
            final Object... statements
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = processPreparedStatement(connection, creator, statements)
        ) {
            return executor.execute(preparedStatement);
        } catch (final SQLException e) {
            throw new PreparedStatementTemplateException(e);
        }
    }

    private PreparedStatement processPreparedStatement(
            final Connection connection,
            final PreparedStatementCreator creator,
            final Object... statements
    ) throws SQLException {
        return bindStatements().bind(creator.create(connection), statements);
    }

    private PreparedStatementBinder bindStatements() {
        return (preparedStatement, statements) -> {
            for (int i = START_STATEMENT_INDEX; i < statements.length + 1; i++) {
                preparedStatement.setObject(i, statements[i - START_STATEMENT_INDEX]);
            }

            return preparedStatement;
        };
    }
}
