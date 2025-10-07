package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.OverSizeResultDataAccessException;
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

    private <T> T execute(SqlExecutor<T> executor) {
        try(Connection conn = dataSource.getConnection()) {
            return executor.execute(conn);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void executeUpdate(String sql, Object ... params) {
        Object[] safeParams = (params == null) ? new Object[0] : params;

        execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                log.debug("query : {}", sql);

                for (int i = 0; i < safeParams.length; i++) {
                    pstmt.setObject(i + 1, safeParams[i]);
                }
                pstmt.executeUpdate();
                return null;
            }
        } );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                log.debug("query : {}", sql);

                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        Object[] safeParams = (params == null) ? new Object[0] : params;

        return execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                log.debug("query : {}", sql);
                for (int i = 0; i < safeParams.length; i++) {
                    pstmt.setObject(i + 1, safeParams[i]);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new EmptyResultDataAccessException();
                    }
                    T result = rowMapper.mapRow(rs);
                    if (rs.next()) {
                        throw new OverSizeResultDataAccessException();
                    }
                    return result;
                }
            }
        });
    }

}
