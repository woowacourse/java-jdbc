package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class QueryExecutorService {

    private final DataSource dataSource;

    public QueryExecutorService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final QueryExecutor<T> queryExecutor, final String query,
                         final Object... columns) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (
                final PreparedStatement pstmt = getPreparedstatement(connection, query, columns);
        ) {
            return queryExecutor.execute(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeAutoCommitConnection(connection);
        }
    }

    private void closeAutoCommitConnection(final Connection connection) {
        try {
            if (connection.getAutoCommit()) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedstatement(final Connection connection, final String query,
                                                   final Object[] columns)
            throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(query);
        setParameters(preparedStatement, columns);
        return preparedStatement;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] columns) throws SQLException {
        for (int i = 0; i < columns.length; i++) {
            pstmt.setObject(i + 1, columns[i]);
        }
    }
}
