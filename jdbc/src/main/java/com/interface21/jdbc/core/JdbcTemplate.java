package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void executeUpdate(String sql, PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pss.setValue(pstmt);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T executeSelect(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(pss, pstmt);
        ) {

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;

        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> executeSelectAll(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery();
        ) {

            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;

        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatementSetter pss, PreparedStatement pstmt) throws SQLException {
        pss.setValue(pstmt);
        return pstmt.executeQuery();
    }
}
