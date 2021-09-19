package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class JdbcTemplate {

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(User user, PreparedStatement pstmt) throws SQLException;

    public void update(User user) {
        String sql = createQuery();
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValues(user, pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
