package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.IncorrectQueryArgumentException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int execute(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = setPreparedStatement(conn, sql, args)) {
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = setPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            final List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = setPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
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
}
