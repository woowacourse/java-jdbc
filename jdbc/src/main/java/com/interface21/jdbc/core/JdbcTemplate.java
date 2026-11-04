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
        return update(sql, argumentSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pstmtSetter) {
        return execute(sql, pstmtSetter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, argumentSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter pstmtSetter, final RowMapper<T> rowMapper) {
        return execute(sql, pstmtSetter, pstmt -> mapRows(pstmt, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, argumentSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter pstmtSetter, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, pstmtSetter, rowMapper);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("조회 결과가 1개보다 많습니다. size=" + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pstmtSetter, final StatementCallback<T> callback) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmtSetter.setValues(pstmt);
            return callback.doInStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapRows(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        }
    }

    private PreparedStatementSetter argumentSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T doInStatement(final PreparedStatement pstmt) throws SQLException;
    }
}
