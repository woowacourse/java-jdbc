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

    public <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback, final Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatementArguments(pstmt, args);
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int parameterIndex = 1; parameterIndex <= args.length; parameterIndex++) {
            pstmt.setObject(parameterIndex, args[parameterIndex - 1]);
        }
    }
}
