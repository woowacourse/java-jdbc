package com.interface21.jdbc.core;

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

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setPreparedStatement(pstmt, args);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapper.mapRow(rs, 1);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("DB 조회에 실패했습니다. :" + sql, e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            final ResultSet rs = pstmt.executeQuery();

            final List<T> result = new ArrayList<>();
            int rowNum = 1;
            while (rs.next()) {
                result.add(mapper.mapRow(rs, rowNum++));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("DB 조회에 실패했습니다. :" + sql, e);
        }
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setPreparedStatement(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB 조회에 실패했습니다. :" + sql, e);
        }
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object[] args) {
        int paramCount = 0;
        try {
            paramCount = pstmt.getParameterMetaData().getParameterCount();
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("SQL 파라미터 개수 불일치: expected " + paramCount + ", actual " + args.length);
        }
    }
}
