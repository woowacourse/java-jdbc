package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplateBase {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateBase.class);
    private final DataSource dataSource;

    public JdbcTemplateBase(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void executionBaseWithNonReturn(final String sql, final JdbcTemplateVoidExecution execution) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            execution.execute(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T executionBaseWithReturn(final String sql, final JdbcTemplateExecution<T> execution) {

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            
            return execution.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
