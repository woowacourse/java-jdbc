package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, argumentsSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, argumentsSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return mapRows(pstmt, rowMapper);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, argumentsSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        final List<T> results = query(sql, rowMapper, pss);
        if (results.size() != 1) {
            throw new DataAccessException("조회 결과가 1건이 아닙니다. 결과 수: " + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapRows(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    private PreparedStatementSetter argumentsSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
