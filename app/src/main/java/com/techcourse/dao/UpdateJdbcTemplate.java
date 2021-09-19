package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UpdateJdbcTemplate.class);
    private static final String QUERY_LOG = "query : {}";

    public void update(final User user, final UserDao userDao) {
        try (Connection conn = userDao.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQueryForUpdate())) {
            setValueForUpdate(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleUserDaoException(e);
        }
    }

    private String createQueryForUpdate() {
        String sql = "update users set account=?, password=?, email=? where id = ?";
        log.debug(QUERY_LOG, sql);
        return sql;
    }

    private void setValueForUpdate(final User user, final PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }

    private void handleUserDaoException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new UserDaoException(e);
    }
}
