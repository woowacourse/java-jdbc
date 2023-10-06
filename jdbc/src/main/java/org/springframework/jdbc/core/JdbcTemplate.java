package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(connection -> prepareStatement(sql, connection, args), PreparedStatement::executeUpdate);
    }

    private <T> T execute(final PreparedStatementCallback preparedStatementCallback,
                         final ExecutionCallback<T> executionCallback) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCallback.prepareStatement(connection)) {
            return executionCallback.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement prepareStatement(final String sql, final Connection connection, final Object[] args)
            throws SQLException {
        final PreparedStatement pstmt = connection.prepareStatement(sql);
        setParameters(pstmt, args);
        return pstmt;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = executeQuery(connection -> prepareStatement(sql, connection, args), rowMapper);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private <T> List<T> executeQuery(
            final PreparedStatementCallback preparedStatementCallback,
            final RowMapper<T> rowMapper
    ) {
        return execute(preparedStatementCallback, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(connection -> prepareStatement(sql, connection, args), rowMapper);
    }
}
