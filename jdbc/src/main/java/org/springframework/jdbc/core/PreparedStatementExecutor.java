package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class PreparedStatementExecutor {

    private static final Logger log = LoggerFactory.getLogger(PreparedStatementExecutor.class);

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final PreparedStatementCallback<T> preparedStatementCallback, final String sql) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return preparedStatementCallback.call(preparedStatement);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);

        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void prepareStatementWithBindingQuery(final PreparedStatement preparedStatement, final Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }
}
