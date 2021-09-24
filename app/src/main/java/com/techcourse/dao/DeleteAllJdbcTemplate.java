package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAllJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteAllJdbcTemplate.class);

    public int deleteAll(DataSource dataSource) {
        try (PreparedStatement pstmt = statementForDeleteAll(dataSource.getConnection())) {
            LOG.debug("query : {}", queryForDeleteAll());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement statementForDeleteAll(Connection connection) throws SQLException {
        return connection.prepareStatement(queryForDeleteAll());
    }

    public String queryForDeleteAll() {
        return "delete from users";
    }
}
