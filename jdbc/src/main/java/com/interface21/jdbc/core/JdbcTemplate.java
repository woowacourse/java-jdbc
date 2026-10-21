package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(final String sql, final Object... args) {
        return update(sql, argumentSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, argumentSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, argumentSetter(args));
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper,
                                          final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                final T result = rowMapper.mapRow(rs);
                if (rs.next()) {
                    throw new DataAccessException("결과가 1건이어야 하지만 2건 이상 조회되었습니다.");
                }
                return Optional.of(result);
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (pss != null) {
                pss.setValues(pstmt);
            }
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Sql 실행에 실패했습니다." + sql, e);
        }
    }

    private PreparedStatementSetter argumentSetter(final Object... args) {
        return pstmt -> {
            if (args == null) {
                return;
            }
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
