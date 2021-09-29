package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UpdateJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(User user) throws SQLException {
        final String sql = createQueryForUpdate();

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {
            log.debug("query : {}", sql);

            setValuesForUpdate(user, pstmt);
            pstmt.executeUpdate();
        }
    }

    private String createQueryForUpdate() {
        final String sql = "update users set account = ?, password = ?, email = ?  where id = ?";
        return sql;
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }

}
