package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementCreator strategy) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = strategy.create(connection)
        ) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> strategy, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = PreparedStatementSetter.setArguments(connection.prepareStatement(sql), args)) {
            ResultSet resultSet = pstmt.executeQuery();
            return getResults(strategy, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResults(RowMapper<T> strategy, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(strategy.map(resultSet));
        }
        return Collections.unmodifiableList(results);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> strategy, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = PreparedStatementSetter.setArguments(connection.prepareStatement(sql), args)) {
            ResultSet resultSet = pstmt.executeQuery();
            return getResult(strategy, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<T> getResult(RowMapper<T> strategy, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return Optional.ofNullable(strategy.map(resultSet));
        }
        return Optional.empty();
    }
}
