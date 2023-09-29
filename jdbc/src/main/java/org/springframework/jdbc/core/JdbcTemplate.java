package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments)
        ) {
            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error create SQL statement: " + sql, e);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error executing SQL query: " + sql, e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments);
                ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.ofNullable(rowMapper.mapRow(rs));
            }

            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error create SQL statement: " + sql, e);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error executing SQL query: " + sql, e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments);
                ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }

            return result;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error create SQL statement: " + sql, e);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error executing SQL query: " + sql, e);
        }
    }
}
