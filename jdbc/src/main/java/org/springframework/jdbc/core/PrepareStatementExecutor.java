package org.springframework.jdbc.core;

import static java.util.Objects.isNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class PrepareStatementExecutor {

    private static final Logger log = LoggerFactory.getLogger(PrepareStatementExecutor.class);

    private final DataSource dataSource;

    public PrepareStatementExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            final PreparedStatementProcessor<T> processor,
            final String sql,
            final Object... params
    ) {
        try (final var connection = getConnection();
             final var pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, params);

            return processor.process(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (isNull(connection)) {
            connection = dataSource.getConnection();
        }
        return connection;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
