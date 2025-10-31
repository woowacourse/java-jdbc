package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
     * INSERT, UPDATE, DELETE 쿼리를 위한 메서드 (수동 파라미터 설정)
     */
    public int update(final String sql, final PreparedStatementSetter pss) {
        return executeUpdate(sql, pss);
    }

    /**
     * INSERT, UPDATE, DELETE 쿼리를 위한 메서드 (자동 파라미터 설정)
     */
    public int update(final String sql, final Object... params) {
        return executeUpdate(sql, createPss(params));
    }

    /**
     * 다건 조회를 위한 메서드 (수동 파라미터 설정)
     */
    public <T> List<T> queryForList(final String sql, final RowMapper<T> mapper, final PreparedStatementSetter pss) {
        return executeQuery(sql, pss, mapper);
    }

    /**
     * 다건 조회를 위한 메서드 (자동 파라미터 설정)
     */
    public <T> List<T> queryForList(final String sql, final RowMapper<T> mapper, final Object... params) {
        return executeQuery(sql, createPss(params), mapper);
    }

    /**
     * 단건 조회를 위한 메서드 (수동 파라미터 설정)
     */
    public <T> Optional<T> queryForObject(
            final String sql,
            final RowMapper<T> mapper,
            final PreparedStatementSetter pss) {
        final List<T> results = queryForList(sql, mapper, pss);

        return processSingleResult(results);
    }

    /**
     * 단건 조회를 위한 메서드 (자동 파라미터 설정)
     */
    public <T> Optional<T> queryForObject(
            final String sql,
            final RowMapper<T> mapper,
            final Object... params) {
        final List<T> results = queryForList(sql, mapper, params);

        return processSingleResult(results);
    }

    private int executeUpdate(final String sql, final PreparedStatementSetter pss) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (final PreparedStatement ps = createPreparedStatement(conn, sql, pss)) {
                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new DataAccessException("SQL 실행 실패", e);
        } finally {
            try {
                DataSourceUtils.releaseConnection(conn, this.dataSource);
            } catch (SQLException e) {
                log.error("Connection 릴리즈 실패", e);
            }
        }
    }

    private <T> List<T> executeQuery(final String sql, final PreparedStatementSetter pss, final RowMapper<T> mapper) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (final PreparedStatement ps = createPreparedStatement(conn, sql, pss);
                 final ResultSet rs = ps.executeQuery()) {

                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.mapRowToObject(rs));
                }
                return results;
            }
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new DataAccessException("SQL 실행 실패", e);
        } finally {
            try {
                DataSourceUtils.releaseConnection(conn, this.dataSource);
            } catch (SQLException e) {
                log.error("Connection 릴리즈 실패", e);
            }
        }
    }

    private <T> Optional<T> processSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("단건 조회인데 결과가 " + results.size() + "건 이상입니다.");
        }

        return Optional.of(results.getFirst());
    }

    private PreparedStatement createPreparedStatement(
            final Connection conn,
            final String sql,
            final PreparedStatementSetter pss) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(sql);
        log.debug("query : {}", sql);
        pss.setValues(ps);

        return ps;
    }

    private PreparedStatementSetter createPss(final Object... params) {
        return ps -> {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        };
    }

    private Connection getConnection() {
        try {
            return DataSourceUtils.getConnection(this.dataSource);
        } catch (final SQLException e) {
            log.error("DB 연결 실패: ", e);
            throw new DataAccessException("DB 연결 실패", e);
        }
    }
}
