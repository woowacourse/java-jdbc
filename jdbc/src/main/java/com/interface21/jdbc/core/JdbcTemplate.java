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
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        return execute(sql, setter, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter setter, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, setter, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("조회 결과가 없습니다. sql : " + sql);
        }
        if (results.size() > 1) {
            throw new DataAccessException("조회 결과가 1건이 아닙니다. size : " + results.size() + ", sql : " + sql);
        }
        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter, final PreparedStatementCallback<T> action) {
        log.debug("query : {}", sql);

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다. sql : " + sql, e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(final PreparedStatement pstmt) throws SQLException;
    }
}
