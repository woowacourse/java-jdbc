package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(String sql, PreparedStatementSetter setter) {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object query(String sql, PreparedStatementSetter setter, RowMapper rowMapper) {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            ResultSet rs = executeQuery(setter, pstmt);
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatementSetter setter, PreparedStatement pstmt) throws SQLException {
        setter.setValues(pstmt);
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();
}
