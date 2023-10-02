package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getInitializedPstmt(sql, conn, args)) {

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getInitializedPstmt(String sql, Connection conn, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);

        initializePstmtArgs(pstmt, args);

        return pstmt;
    }

    private void initializePstmtArgs(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getInitializedPstmt(sql, conn, args);
                ResultSet rs = pstmt.executeQuery()) {

            List<T> results = new ArrayList<>();

            while (rs.next()) {
                T result = rowMapper.mapRow(rs);

                results.add(result);
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getInitializedPstmt(sql, conn, args);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                T result = rowMapper.mapRow(rs);

                validateMultipleResults(rs);

                return result;
            }

            throw new DataAccessException("Incorrect Result Size ! Result is null");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void validateMultipleResults(ResultSet rs) throws SQLException {
        if (rs.next()) {
            throw new DataAccessException("Incorrect Result Size ! Result  must be one");
        }
    }

}
