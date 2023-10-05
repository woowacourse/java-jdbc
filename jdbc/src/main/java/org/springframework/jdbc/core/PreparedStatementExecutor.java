package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementExecutor {

    private final DataSource dataSource;

    public PreparedStatementExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final PreparedStatementGenerator psmtGenerator, final PreparedStatementCaller<T> psmtCaller) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = psmtGenerator.generate(conn)) {
            return psmtCaller.call(psmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T execute(
            final Connection conn,
            final PreparedStatementGenerator psmtGenerator,
            final PreparedStatementCaller<T> psmtCaller
    ) {
        try (final PreparedStatement psmt = psmtGenerator.generate(conn)) {
            return psmtCaller.call(psmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
