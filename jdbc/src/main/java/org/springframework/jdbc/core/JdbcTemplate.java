package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.IncorrectQueryArgumentException;
import org.springframework.jdbc.exception.IncorrectResultSizeDataAccessException;

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

    public int execute(final Connection conn, final String sql, final Object... args) {
        return manageData(conn, PreparedStatement::executeUpdate, sql, args);
    }

    public <T> List<T> query(final Connection conn, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return manageData(conn, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        }, sql, args);
    }

    public <T> T queryForObject(final Connection conn, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return manageData(conn, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return getOneResult(rs, rowMapper);
            }
        }, sql, args);
    }

    private <T> T manageData(final Connection conn, final PreparedStatementImpl<T> qm, final String sql, final Object... args) {
        try (PreparedStatement pstmt = setPreparedStatement(conn, sql, args);) {
            log.debug("query : {}", sql);
            return qm.callback(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setPreparedStatement(final Connection conn, final String sql, final Object... args)
            throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        validateArgsCount(sql, args.length);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }

    private void validateArgsCount(final String str, final int argsCount) {
        final long questionMarksCount = str.chars()
                .filter(c -> c == '?')
                .count();

        if (questionMarksCount > argsCount) {
            throw new IncorrectQueryArgumentException("delivered parameter's count is deficient.");
        }

        if (questionMarksCount < argsCount) {
            throw new IncorrectQueryArgumentException("delivered parameter's count is many.");
        }
    }

    private <T> T getOneResult(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        if (rs.next()) {
            T result = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new IncorrectResultSizeDataAccessException("More than one result to return");
            }
            return result;
        }
        throw new IncorrectResultSizeDataAccessException("No result to return");
    }
}
