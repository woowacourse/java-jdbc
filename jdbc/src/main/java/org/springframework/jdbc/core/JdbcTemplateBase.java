package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.error.SqlExceptionConverter;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplateBase {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateBase.class);
    private final DataSource dataSource;

    public JdbcTemplateBase(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executionBaseWithNonReturn(final String sql, final JdbcTemplateVoidExecution execution) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            execution.execute(preparedStatement);
        } catch (SQLException e) {
            throw SqlExceptionConverter.convert(e);
        }
    }

    public <T> T executionBaseWithReturn(final String sql, final JdbcTemplateExecutor<T> execution) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return execution.execute(preparedStatement);
        } catch (SQLException e) {
            throw SqlExceptionConverter.convert(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
