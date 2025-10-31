package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            log.debug("query: {}", sql);
            pss.setValues(pstmt);
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("Result가 여러 개 입니다." + results.size());
        }
        return results.get(0);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                log.debug("query: {}", sql);
                return action.doInPreparedStatement(pstmt);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
