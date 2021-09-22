package com.techcourse.dao.jdbc;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.dao.UpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateJdbcTemplate.class);

    public void update(User user, UserDao userDao) {
        final String sql = createQueryForUpdate();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getDataSource(userDao).getConnection();
            pstmt = conn.prepareStatement(sql);

            LOG.debug("query: {}", sql);

            setValuesForUpdate(user, pstmt);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new UpdateException();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private String createQueryForUpdate() {
        return "update users set password = ? where id = ?";
    }

    private DataSource getDataSource(UserDao userDao) {
        return userDao.getDataSource();
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getPassword());
        pstmt.setLong(2, user.getId());
    }
}
