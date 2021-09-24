package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, args);
    }

    private int execute(String sql, Object[] args) {
        LOG.debug("query : {}", sql);

        try (PreparedStatement statement = createStatement(sql, args)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // TODO: dataSource.getConnection과 connection.prepareStatement가 try-with-resources 의 수혜를 받는가?
    public PreparedStatement createStatement(String sql, Object[] args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }

        return statement;
    }
}
