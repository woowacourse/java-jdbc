package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.DataAccessUtils;
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            setParameters(pstmt, args);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException exception) {
            log.error("쿼리 실행 중 에러가 발생했습니다.", exception);
            throw new DataAccessException("쿼리 실행 에러 발생", exception);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException exception) {
            log.error("쿼리 실행 중 에러가 발생했습니다.", exception);
            throw new DataAccessException("쿼리 실행 에러 발생", exception);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
