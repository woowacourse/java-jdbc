package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter setter) {
        execute(sql, setter, PreparedStatement::executeUpdate);
    }

    /**
     * 단일 SQL 인자로 NULL을 전달할 때는 {@code update(sql, new Object[]{null})}을 사용한다.
     * {@code update(sql, null)}은 PreparedStatementSetter 오버로드와 구분할 수 없다.
     */
    public void update(String sql, Object... args) {
        update(sql, pstmt -> bindParameters(pstmt, args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, pstmt -> bindParameters(pstmt, args), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        return execute(sql, setter, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    private <R> R execute(String sql, PreparedStatementSetter setter, PreparedStatementCallback<R> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            setter.setValues(pstmt);

            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<R> {
        R doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
    }
}
