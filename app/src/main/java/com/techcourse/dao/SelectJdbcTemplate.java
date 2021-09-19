package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class SelectJdbcTemplate {

    protected abstract String createQuery();

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

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

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
