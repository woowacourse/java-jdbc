package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public int update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            processPreparedStatementParameter(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void processPreparedStatementParameter(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        final int argsLength = args.length;
        for (int i = 0; i < argsLength; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMaper, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            processPreparedStatementParameter(pstmt, args);

            List<T> results = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(rowMaper.mapRow(rs));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            processPreparedStatementParameter(pstmt, args);

            ResultSet rs = pstmt.executeQuery();

            T findObject = null;
            if (rs.next()) {
                findObject = rowMapper.mapRow(rs);
            }

            validateSingleResult(findObject, rs);

            return findObject;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> void validateSingleResult(final T findObject, final ResultSet rs) throws SQLException {
        if (findObject == null) {
            throw new IncorrectResultSizeDataAccessException(0);
        }
        if (rs.next()) {
            rs.last();
            throw new IncorrectResultSizeDataAccessException(rs.getRow());
        }
    }
}
