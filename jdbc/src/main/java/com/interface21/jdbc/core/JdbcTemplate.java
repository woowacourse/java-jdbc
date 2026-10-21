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

    public <T> T query(final String sql, final PreparedStatementSetter pstmtSetter,
        final RowMapper<T> rowMapper) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            final ResultSet resultSet = executeQuery(pstmtSetter, pstmt);

            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            throw new IllegalStateException("query failed", e);
        }
    }

    public void update(final String sql, final PreparedStatementSetter pstmtSetter) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            executeUpdate(pstmtSetter, pstmt);
        } catch (SQLException e) {
            throw new IllegalStateException("update failed", e);
        }
    }

    private ResultSet executeQuery(
        final PreparedStatementSetter pstmtSetter,
        final PreparedStatement pstmt
    ) throws SQLException {
        pstmtSetter.setValues(pstmt);

        return pstmt.executeQuery();
    }

    private void executeUpdate(
        final PreparedStatementSetter pstmtSetter,
        final PreparedStatement pstmt
    ) throws SQLException {
        pstmtSetter.setValues(pstmt);
        pstmt.executeUpdate();
    }
}
