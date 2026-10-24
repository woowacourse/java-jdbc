package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
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
        return update(sql, createArgumentSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createArgumentSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (final var rs = pstmt.executeQuery()) {
                final var results = new ArrayList<T>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, createArgumentSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        final var results = query(sql, rowMapper, pss);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("조회 결과가 1개보다 많습니다. size: " + results.size() + ", sql: " + sql);
        }
        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss, final PreparedStatementCallback<T> action) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            final var message = "SQL 실행 중 오류가 발생했습니다. sql: " + sql;
            log.error(message, e);
            throw new DataAccessException(message, e);
        }
    }

    private PreparedStatementSetter createArgumentSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
