package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.StatementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindAllStatement implements StatementStrategy {
    private static final Logger log = LoggerFactory.getLogger(FindAllStatement.class);

    @Override
    public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
        final String sql = "select id, account, password, email from users";

        final PreparedStatement pstmt = connection.prepareStatement(sql);

        log.info("query : {}", sql);

        return pstmt;
    }
}
