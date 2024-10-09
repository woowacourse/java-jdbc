package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.exception.JdbcSQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final int START_ARGUMENT_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        log.debug("update query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(arguments, pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = String.format("Error executing update: %s with arguments: %s", sql,
                    Arrays.toString(arguments));
            log.error(errorMessage);
            throw new JdbcSQLException(errorMessage, e);
        }
    }

    public <T> T queryObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        List<T> result = query(sql, rowMapper, arguments);
        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        log.debug("query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(arguments, pstmt);

            ResultSet rs = pstmt.executeQuery();
            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rowMapper.mapRow(rs, rs.getFetchSize()));
            }
            return objects;
        } catch (SQLException e) {
            String errorMessage = String.format("Error executing query: %s with arguments: %s", sql,
                    Arrays.toString(arguments));
            log.error(errorMessage);
            throw new JdbcSQLException(errorMessage, e);
        }
    }

    private void setArguments(Object[] arguments, PreparedStatement pstmt) throws SQLException {
        int count = START_ARGUMENT_COUNT;
        for (Object argument : arguments) {
            pstmt.setObject(count, argument);
            count++;
        }
    }
}
