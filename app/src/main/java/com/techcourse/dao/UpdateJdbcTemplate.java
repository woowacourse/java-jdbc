package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UpdateJdbcTemplate.class);

    public void update(User user, JdbcUserDao userDao) {
        String sql = createQueryForUpdate();

        try (Connection conn = userDao.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setValuesForUpdate(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }

    private String createQueryForUpdate() {
        return "UPDATE users SET account=?, password=?, email=? WHERE id=?";
    }
}
