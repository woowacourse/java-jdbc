package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(
            final String sql,
            final Object... args
    ) {
        executeUpdate(sql, args);
    }

    public int update(
            final String sql,
            final Object... args
    ) {
        return executeUpdate(sql, args);
    }

    public <T> List<T> findAll(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return executeQuery(sql, args, rs -> {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        });
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return executeQuery(sql, args, rs -> {
            if (!rs.next()) {
                throw new DataAccessException("조회 결과가 존재하지 않습니다.");
            }
            T result = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new DataAccessException("조회 결과가 2개 이상입니다. SQL: " + sql);
            }
            return result;
        });
    }

    private <T> T executeQuery(
            final String sql,
            final Object[] args,
            final SqlFunction<ResultSet, T> extractor
    ) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (
                    final PreparedStatement ps = createPreparedStatement(con, sql, args);
                    final ResultSet rs = ps.executeQuery()
            ) {
                return extractor.apply(rs);
            }
        } catch (final SQLException e) {
            log.error("쿼리 실행 중 오류 발생: {}", sql, e);
            throw new DataAccessException("데이터를 조회하는 중 오류가 발생했습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private int executeUpdate(
            final String sql,
            final Object... args
    ) {
        Connection con = null;
        try {
            con = DataSourceUtils.getConnection(dataSource);
            try (final PreparedStatement ps = createPreparedStatement(con, sql, args)) {
                return ps.executeUpdate();
            }
        } catch (final SQLException e) {
            log.error("쿼리 실행 중 오류 발생: {}", sql, e);
            throw new DataAccessException("데이터를 수정하는 중 오류가 발생했습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private PreparedStatement createPreparedStatement(
            final Connection con,
            final String sql,
            final Object[] args
    ) throws SQLException {
        validateParameterCount(sql, args);
        final PreparedStatement ps = con.prepareStatement(sql);
        log.debug("query : {}", sql);
        log.trace("parameters : {}", Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        return ps;
    }

    private void validateParameterCount(
            final String sql,
            final Object[] args
    ) {
        final long placeholderCount = sql.chars()
                .filter(ch -> ch == '?')
                .count();
        if (placeholderCount != args.length) {
            throw new DataAccessException("SQL 파라미터 개수 불일치: ? %d개, 인자 %d개".formatted(placeholderCount, args.length));
        }
    }
}
