package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertStatement implements StatementStrategy {
    private static final Logger log = LoggerFactory.getLogger(InsertStatement.class);

    final User user;

    public InsertStatement(User user) {
        this.user = user;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        final PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());

        log.info("query : {}", sql);

        return pstmt;
    }
}
