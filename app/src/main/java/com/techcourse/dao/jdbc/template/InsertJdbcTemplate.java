package com.techcourse.dao.jdbc.template;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
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
        final DataSource dataSource = userDao.getDataSource();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }
}
