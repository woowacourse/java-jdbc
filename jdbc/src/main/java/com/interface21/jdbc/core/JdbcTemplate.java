package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate implements JdbcOperations {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeStatement(sql, (pstmt) -> executeQueryAndGet(pstmt, rowMapper));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        return executeStatement(sql, (pstmt) -> {
            bindValues(pstmt, values);
            return executeQueryAndGet(pstmt, rowMapper);
        });
    }

    private <T> T executeStatement(String sql, PreparedStatementCallBack<T> preCallBack) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return preCallBack.callback(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> executeQueryAndGet(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
        }
        return result;
    }

    private void bindValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int idx = 1; idx <= values.length; idx++) {
            pstmt.setObject(idx, values[idx - 1]);
        }
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... values) {
        RowMapper<T> rowMapper = RowMapperFactory.getRowMapper(clazz);
        List<T> result = query(sql, rowMapper, values);
        validateSingleResult(result);
        return result.getFirst();
    }

    private <T> void validateSingleResult(List<T> result) {
        if (result.size() != 1) {
            throw new IllegalStateException("결과가 1개만 조회되어야 하지만, " + result.size() + "개의 결과가 조회되었습니다: " + result);
        }
    }

    public void update(String sql, Object... values) {
        executeStatement(sql, (pstmt) -> {
            bindValues(pstmt, values);
            return pstmt.executeUpdate();
        });
    }

    public void execute(String sql) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
