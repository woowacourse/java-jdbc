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

    public int update(final String sql, final Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return queryForObject(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, pss, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("Incorrect result size: expected 1, actual 0");
        }
        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return pstmt -> setParameters(pstmt, args);
    }

    private void setParameters(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
