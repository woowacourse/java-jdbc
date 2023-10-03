package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, sql, arguments)) {

            log.info(sql);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, sql, arguments)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new IllegalArgumentException("결과가 없습니다");
            }
            return rowMapper.map(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... arguments) {
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, sql, arguments)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStatement(Connection connection, String sql, Object... arguments) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < arguments.length; i++) {
            preparedStatement.setObject(i + 1, arguments[i]);
        }
        return preparedStatement;
    }
}
