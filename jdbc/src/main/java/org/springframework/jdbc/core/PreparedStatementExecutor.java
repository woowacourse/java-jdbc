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
            final PreparedStatementProcessor<T> preparedStatementProcessor,
            final String sql,
            final Object... parameters
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = generatePreparedStatement(connection, sql, parameters)
        ) {
            return preparedStatementProcessor.process(preparedStatement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement generatePreparedStatement(
            final Connection connection,
            final String sql,
            final Object... parameters
    ) {
        try {
            log.debug("query : {}", sql);
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);

            setParameters(preparedStatement, parameters);
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            pstmt.setObject(i, parameters[i - 1]);
        }
    }
}
