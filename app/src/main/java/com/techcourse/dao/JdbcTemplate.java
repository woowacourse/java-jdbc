package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class JdbcTemplate {

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public void update() {
        String sql = createQuery();
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValues(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object query() {
        final String sql = createQuery();
        ResultSet rs = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setValues(pstmt);
            rs = executeQuery(pstmt);

            return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}
        }
    }
}
