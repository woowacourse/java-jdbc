package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplateBase {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateBase.class);
    protected static final boolean TRANSACTION_ENABLE = false;
    protected static final boolean TRANSACTION_DISABLE = true;
    private final DataSource dataSource;

    public JdbcTemplateBase(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void executionBaseWithNonReturn(final String sql,
                                              final JdbcTemplateVoidExecution execution,
                                              final boolean enableTransaction) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(!enableTransaction);
            log.debug("query : {}", sql);
            execution.execute(preparedStatement);
            if (connection.getAutoCommit() == TRANSACTION_ENABLE) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T executionBaseWithReturn(final String sql,
                                            final JdbcTemplateExecutor<T> execution,
                                            final boolean enableTransaction) {

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(!enableTransaction);
            log.debug("query : {}", sql);
            final T result = execution.execute(preparedStatement);
            if (connection.getAutoCommit() == TRANSACTION_ENABLE) {
                connection.commit();
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
