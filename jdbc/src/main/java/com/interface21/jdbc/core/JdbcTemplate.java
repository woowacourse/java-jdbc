package com.interface21.jdbc.core;

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

    public void executeUpdate(final String sql, final PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmtSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pstmtSetter) {
        List<T> resultSet = executeQuery(sql, rowMapper, pstmtSetter);
        if (resultSet.size() > 1) {
            throw new IllegalArgumentException("Multiple results returned for query, but only one result expected.");
        }

        return resultSet.stream().findFirst();
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return getResultSet(sql, pstmt, pstmtSetter, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResultSet(final String sql, final PreparedStatement pstmt, final PreparedStatementSetter pstmtSetter, final RowMapper<T> rowMapper) throws SQLException {
        pstmtSetter.setValues(pstmt);
        try (ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }
}
