package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementExecutor {

    private static final Logger log = LoggerFactory.getLogger(PreparedStatementExecutor.class);

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementGenerator preparedStatementGenerator,
            final PreparedStatementProcessor<T> preparedStatementProcessor
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementGenerator.generate(connection)
        ) {
            return preparedStatementProcessor.process(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
