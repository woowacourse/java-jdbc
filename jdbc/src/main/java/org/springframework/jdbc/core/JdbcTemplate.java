package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void execute(String sql) {
        context(connection -> connection.prepareStatement(sql));
    }

    public void execute(String sql, Object... args) {
        context(connection -> connection.prepareStatement(sql), args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rm) {
        return context(connection -> connection.prepareStatement(sql), rm);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rm, Object... args) {
        List<T> list = context(connection -> connection.prepareStatement(sql), rm, args);

        if (list.isEmpty()) {
            return null;
        }

        if (list.size() > 1) {
            throw new DataAccessException("조건에 해당하는 값이 " + list.size() + "개입니다.");
        }

        return list.get(0);
    }

    public void context(PreparedStrategy preparedStrategy) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStrategy.createStatement(conn)) {

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void context(PreparedStrategy preparedStrategy, Object[] args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStrategy.createStatement(conn)) {

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> context(PreparedStrategy preparedStrategy, RowMapper<T> rm) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStrategy.createStatement(conn);
             ResultSet rs = pstmt.executeQuery()) {

            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rm.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> context(PreparedStrategy preparedStrategy, RowMapper<T> rm, Object[] args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStrategy.createStatement(conn)) {

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rm.mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
