package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
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
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            execution.execute(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (Objects.nonNull(preparedStatement)) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
            }
            try {
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    protected <T> T executionBaseWithReturn(final String sql, final JdbcTemplateExecution<T> execution) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            log.debug("query : {}", sql);
            return execution.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
