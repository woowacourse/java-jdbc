package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(InsertJdbcTemplate.class);

    public void insert(User user, JdbcUserDao userDao) {
        try (Connection conn = userDao.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQueryForInsert())) {
            log.debug("query : {}", createQueryForInsert());
            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    private String createQueryForInsert() {
        return "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
    }
}
