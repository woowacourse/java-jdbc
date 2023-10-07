package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class QueryExecutorService {

    private final DataSource dataSource;

    public QueryExecutorService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final Connection connection, final QueryExecutor<T> queryExecutor, final String query,
                         final Object... columns) {
        try (
                final PreparedStatement pstmt = getPreparedstatement(connection, query, columns);
        ) {
            return queryExecutor.execute(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedstatement(final Connection conn, final String query, final Object[] columns)
            throws SQLException {
        final PreparedStatement preparedStatement = conn.prepareStatement(query);
        setParameters(preparedStatement, columns);
        return preparedStatement;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] columns) throws SQLException {
        for (int i = 0; i < columns.length; i++) {
            pstmt.setObject(i + 1, columns[i]);
        }
    }
}
