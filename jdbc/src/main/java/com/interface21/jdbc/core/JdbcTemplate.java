package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
        return update(sql, argumentSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public int update(final Connection conn, final String sql, final Object... args) {
        return execute(conn, sql, argumentSetter(args), PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, argumentSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, resultSetExtractor(rowMapper));
    }

    public <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> rowMapper,
                             final Object... args) {
        return execute(conn, sql, argumentSetter(args), resultSetExtractor(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, argumentSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return singleResult(query(sql, rowMapper, pss));
    }

    public <T> T queryForObject(final Connection conn, final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return singleResult(query(conn, sql, rowMapper, args));
    }

    private <T> T singleResult(final List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException("조회 결과가 1개가 아닙니다. size = " + results.size());
        }
        return results.get(0);
    }

    private <T> PreparedStatementCallback<List<T>> resultSetExtractor(final RowMapper<T> rowMapper) {
        return pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> callback) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return execute(conn, sql, pss, callback);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> callback) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            if (pss != null) {
                pss.setValues(pstmt);
            }
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
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
}
