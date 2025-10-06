package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try {
            execute(sql, PreparedStatement::executeUpdate, buildPreparedStatementSetter(args), args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(RowMapper<T> rowMapper, String sql, Object... args) {
        try {
            return execute(sql, pstmt -> {
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rowMapper.mapRow(rs) : null;
            }, buildPreparedStatementSetter(args), args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(RowMapper<T> rowMapper, String sql, Object... args) {
        try {
            return execute(sql, pstmt -> {
                List<T> result = new ArrayList<>();
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }, buildPreparedStatementSetter(args), args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> preparedStatementCallback,
                          PreparedStatementSetter preparedStatementSetter, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(pstmt);
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatementSetter buildPreparedStatementSetter(Object... args) throws SQLException {
        return (preparedStatement) -> {
            if (args == null || args.length == 0) {
                return;
            }

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }
}
