package com.interface21.jdbc.core;

import com.interface21.rowMapper.RowMapper;
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

    /**
     * INSERT, UPDATE, DELETE 전용 메서드
     * @return 영향 받은 행(row) 수
     */
    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            for (int i=0; i<params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * SELECT 전용 메서드
     * 여러 행을 반환
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            for (int i=0; i<params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * SELECT 단건 조회 전용 메서드
     * 결과가 없을 시 null을 반환
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> queryResult = query(sql, rowMapper, params);
        if (queryResult.isEmpty()) {
            return null;
        }
        return queryResult.getFirst();
    }
}
