package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    protected abstract DataSource getDatasource();

    protected abstract String createQuery();

    protected abstract void setValues(User user, PreparedStatement pstmt) throws SQLException;

    public void update(User user) throws SQLException {
        final String sql = createQuery();

        Connection conn = getDatasource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {
            log.debug("query : {}", sql);

            setValues(user, pstmt);
            pstmt.executeUpdate();
        }
    }

}
