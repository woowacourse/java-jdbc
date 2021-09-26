package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(nextstep.jdbc.JdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(User user, PreparedStatement pstmt) throws SQLException;

    public void update(User user) {
        String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            setValues(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
