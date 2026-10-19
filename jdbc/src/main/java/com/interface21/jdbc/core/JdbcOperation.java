package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class JdbcOperation<R> {

    private final DataSource dataSource;

    protected JdbcOperation(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final R execute(String sql) {
        return execute(sql, pstmt -> {
        });
    }

    public final R execute(String sql, PreparedStatementSetter setter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setter.setValues(pstmt);
            return executeStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    protected abstract R executeStatement(PreparedStatement pstmt) throws SQLException;
}
