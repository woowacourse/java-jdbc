package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, rowMapper, args);
        return getSingleResult(result);
    }

    private <T> T getSingleResult(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("데이터를 찾을 수 없습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("결과값이 두 개 이상입니다.");
        }
        return result.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            final List<T> result = new ArrayList<>();
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
