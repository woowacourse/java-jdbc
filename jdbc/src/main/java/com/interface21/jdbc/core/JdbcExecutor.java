package com.interface21.jdbc.core;

import com.interface21.jdbc.core.exception.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
    public int executeUpdate(final String sql, final Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    /**
     * select 쿼리 실행용
     */
    public <T> List<T> executeQuery(final RowMapper<T> mapper, final String sql, final Object... args) {
        return execute(pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
                return results;
            }
        }, sql, args);
    }

    /**
     * 공통 쿼리 실행 메서드 - 리소스 관리
     */
    private <R> R execute(final SqlExecutor<R> executor, final String sql, final Object[] args) {
        Connection conn = getConnection();
        try (
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

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}


