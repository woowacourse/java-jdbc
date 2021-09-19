package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(InsertJdbcTemplate.class);
    private static final String QUERY_LOG = "query : {}";

    public void insert(final User user, final UserDao userDao) {
        try (Connection conn = userDao.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQueryForInsert())) {
            setValueForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleUserDaoException(e);
        }
    }

    private String createQueryForInsert() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.debug(QUERY_LOG, sql);
        return sql;
    }

    private void setValueForInsert(final User user, final PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    private void handleUserDaoException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new UserDaoException(e);
    }
}
