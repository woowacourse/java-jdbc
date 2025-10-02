package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final var results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new DataAccessException("result size doesn't match");
        }
        return results.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
                var conn = dataSource.getConnection();
                var pstmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParams(pstmt, args);
            try (var rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                return extractResults(rs, rowMapper);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (
                var conn = dataSource.getConnection();
                var pstmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParams(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementParams(final PreparedStatement pstmt, final Object... args) throws SQLException {
        final int parameterCount = pstmt.getParameterMetaData().getParameterCount();
        if (args.length != parameterCount) {
            throw new DataAccessException();
        }
        for (int i = 0; i < parameterCount; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> extractResults(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }
}
