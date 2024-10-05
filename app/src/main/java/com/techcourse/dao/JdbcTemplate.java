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

    public void update() {
        final var sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object query() {
        final var sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setValues(pstmt);
            ResultSet rs = executeQuery(pstmt);
            if (rs.next()) {
                return mapRow(rs);
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    protected abstract Object mapRow(ResultSet rs) throws SQLException;
}
