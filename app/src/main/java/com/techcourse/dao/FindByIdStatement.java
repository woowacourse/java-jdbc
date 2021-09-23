package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.StatementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindByIdStatement implements StatementStrategy {
    private static final Logger log = LoggerFactory.getLogger(FindByIdStatement.class);

    private final Long id;

    public FindByIdStatement(Long id) {
        this.id = id;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection connection) throws SQLException {
        final String sql = "select id, account, password, email from users where id = ?";

        final PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setLong(1, id);

        log.info("query : {}", sql);

        return pstmt;
    }
}
