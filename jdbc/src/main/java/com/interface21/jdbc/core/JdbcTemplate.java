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
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final Connection conn, final String sql, final Object... args) {
        return update(conn, sql, createSetter(args));
    }

    public int update(final Connection conn, final String sql, final PreparedStatementSetter pss) {
        return execute(conn, sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> Optional<T> queryForObject(final Connection conn, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(conn, sql, rowMapper, createSetter(args));
    }

    public <T> Optional<T> queryForObject(final Connection conn, final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return single(query(conn, sql, rowMapper, pss), sql);
    }

    public <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(conn, sql, rowMapper, createSetter(args));
    }

    public <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(conn, sql, pss, pstmt -> mapRows(pstmt, rowMapper));
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementSetter pss, final PreparedStatementCallback<T> action) {
        log.debug("query : {}", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 실패: " + sql, e);
        }
    }

    private <T> List<T> mapRows(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        }
    }

    private <T> Optional<T> single(final List<T> results, final String sql) {
        if (results.size() > 1) {
            throw new DataAccessException("결과가 1건이어야 하지만 " + results.size() + "건 조회됨: " + sql);
        }
        return results.stream().findFirst();
    }

    private PreparedStatementSetter createSetter(final Object... args) {
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
