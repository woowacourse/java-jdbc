package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("query 실패 {}", sql, e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public List<Object> query(String sql, RowMapper rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, args);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Object> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error("SQL query failed. query: {}", sql, e);
            throw new RuntimeException(e);
        }
    }

    public Object queryForObject(String sql, RowMapper rowMapper, Object... args) {
        List<Object> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
