package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final int SINGLE_RESULT = 1;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameterValue(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);

        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(SINGLE_RESULT);
        }
        if (results.size() > SINGLE_RESULT) {
            throw new IncorrectResultSizeDataAccessException(SINGLE_RESULT, results.size());
        }
        return results.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameterValue(pstmt, args);
            try (final ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);

                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameterValue(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
