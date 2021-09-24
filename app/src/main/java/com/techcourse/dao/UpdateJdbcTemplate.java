package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateJdbcTemplate.class);

    public int update(DataSource dataSource, User user) {
        try (PreparedStatement pstmt = statementForUpdate(dataSource.getConnection(), user)) {
            LOG.debug("query : {}", queryForUpdate());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement statementForUpdate(Connection connection, User user) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(queryForUpdate());

        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());

        return pstmt;
    }

    public String queryForUpdate() {
        return "update users set account=?, password=?, email=? where id=?";
    }
}
