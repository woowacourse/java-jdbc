package com.techcourse.dao.jdbc.template;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
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

        final DataSource dataSource = userDao.getDataSource();
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            setValuesForUpdate(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createQueryForUpdate() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }
}
