package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStatement implements StatementStrategy {
    private static final Logger log = LoggerFactory.getLogger(UpdateStatement.class);

    private final User user;

    public UpdateStatement(User user) {
        this.user = user;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {

        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        final PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());

        log.info("query : {}", sql);

        return pstmt;
    }
}
