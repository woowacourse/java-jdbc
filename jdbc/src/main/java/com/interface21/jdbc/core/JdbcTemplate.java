package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(
            final Connection connection,
            final String sql,
            final Object... args
    ) {
        try (final PreparedStatement ps = createPreparedStatement(connection, sql, args)) {
            return ps.executeUpdate();
        } catch (final SQLException e) {
            log.error("데이터베이스 처리 중 예외 발생", e);
            throw new DataAccessException(e);
        }
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
                throw new DataAccessException("쿼리 결과가 없습니다.");
            }
            final T result = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new DataAccessException("쿼리 결과가 2개 이상입니다. " + result);
            }
            return result;
        });
    }

    private <T> T executeQuery(
            final String sql,
            final Object[] args,
            final SqlFunction<ResultSet, T> mapper
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement ps = createPreparedStatement(connection, sql, args);
                final ResultSet rs = ps.executeQuery()
        ) {
            return mapper.apply(rs);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private int executeUpdate(
            final String sql,
            final Object... args
    ) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement ps = createPreparedStatement(connection, sql, args)
        ) {
            return ps.executeUpdate();
        } catch (final SQLException e) {
            log.error("데이터베이스 처리 중 예외 발생", e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPreparedStatement(
            final Connection con,
            final String sql,
            final Object[] args
    ) throws SQLException {
        final PreparedStatement ps = con.prepareStatement(sql);
        log.debug("query : {}", sql);
        log.trace("parameters : {}", Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        return ps;
    }
}
