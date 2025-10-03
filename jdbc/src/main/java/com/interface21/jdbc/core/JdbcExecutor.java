package com.interface21.jdbc.core;

import com.interface21.jdbc.core.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcExecutor {

    private final DataSource dataSource;

    public JdbcExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * update 쿼리 실행용
     */
    public int executeUpdate(final String sql, final Object[] args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    /**
     * select 쿼리 실행용
     */
    public <T> List<T> executeQuery(final String sql, final Object[] args, final RowMapper<T> mapper) {
        return execute(sql, args, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    /**
     * 공통 쿼리 실행 메서드 - 리소스 관리
     */
    private <R> R execute(final String sql, final Object[] args, final SqlExecutor<R> executor) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    pstmt.setObject(i + 1, args[i]);
                }
            }
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("JDBC 작업 중 오류 발생", e);
        }
    }
}


