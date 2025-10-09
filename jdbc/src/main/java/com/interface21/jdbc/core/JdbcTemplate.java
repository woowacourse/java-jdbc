package com.interface21.jdbc.core;

import com.interface21.jdbc.transaction.ConnectionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, newArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(RowMapper<T> rowMapper, String sql, Object... args) {
        return execute(sql, pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rowMapper.mapRow(rs) : null;
        }, newArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(RowMapper<T> rowMapper, String sql, Object... args) {
        return execute(sql, pstmt -> {
            List<T> result = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        }, newArgumentPreparedStatementSetter(args));
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> preparedStatementCallback,
                          PreparedStatementSetter preparedStatementSetter) {
        Connection conn = ConnectionHolder.getConnection();

        if (conn == null) {
            throw new RuntimeException("connection is null");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(pstmt);
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatementSetter newArgumentPreparedStatementSetter(Object... args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
