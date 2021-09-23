package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.StatementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindByAccountStatement implements StatementStrategy {
    private static final Logger log = LoggerFactory.getLogger(FindByAccountStatement.class);

    private final String account;

    public FindByAccountStatement(String account) {
        this.account = account;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
        final String sql = "select id, account, password, email from users where account = ?";

        final PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, account);

        log.info("query : {}", sql);

        return pstmt;
    }
}
