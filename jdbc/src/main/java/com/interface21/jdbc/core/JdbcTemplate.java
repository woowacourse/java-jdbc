package com.interface21.jdbc.core;

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

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final Connection connection, final String sql, final Object... params) {
        return executeWithPreparedStatement(
                connection,
                sql,
                new SimplePreparedStatementSetter(params),
                PreparedStatement::executeUpdate
        );
    }

    public <T> T queryForObject(
            final Connection connection,
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return executeWithPreparedStatement(connection, sql, new SimplePreparedStatementSetter(params), (pstmt -> {

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rowMapper.map(rs);
                }
                return null;
            }
        }));
    }

    public <T> List<T> query(
            final Connection connection,
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... params
    ) {
        return executeWithPreparedStatement(
                connection,
                sql,
                new SimplePreparedStatementSetter(params),
                pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<T> results = new ArrayList<>();

                        while (rs.next()) {
                            results.add(rowMapper.map(rs));
                        }

                        return results;
                    }
                }
        );
    }

    private <T> T executeWithPreparedStatement(
            final Connection connection,
            final String sql,
            final PreparedStatementSetter pstmtSetter,
            final PreparedStatementAction<T> action
    ) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmtSetter.setValues(pstmt);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
