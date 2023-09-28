package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            initializePstmtArgs(pstmt, args);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void initializePstmtArgs(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            List<T> results = new ArrayList<>();

            while (rs.next()) {

                T result = rowMapper.mapRow(rs);

                results.add(result);
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getQueryPstmtForObject(sql, conn, args);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                T result = rowMapper.mapRow(rs);

                if (rs.next()) {
                    throw new IllegalArgumentException("Incorrect Result Size ! Result  must be one");
                }

                return result;
            }

            throw new NoSuchElementException("Incorrect Result Size ! Result is null");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getQueryPstmtForObject(String sql, Connection conn, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);

        initializePstmtArgs(pstmt, args);

        return pstmt;
    }

}
