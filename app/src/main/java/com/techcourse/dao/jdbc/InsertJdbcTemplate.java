package com.techcourse.dao.jdbc;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.dao.InsertException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(InsertJdbcTemplate.class);

    public void insert(User user, UserDao userDao) {
        final String sql = createQueryForInsert();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getDataSource(userDao).getConnection();
            pstmt = conn.prepareStatement(sql);

            LOG.debug("query: {}", sql);

            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new InsertException();
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

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private DataSource getDataSource(UserDao userDao) {
        return userDao.getDataSource();
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }
}
