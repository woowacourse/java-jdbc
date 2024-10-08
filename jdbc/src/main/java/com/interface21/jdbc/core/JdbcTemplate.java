package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void execute(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            log.debug("query : {}", sql);

            statement.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new DataAccessException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection, sql, args)) {
            log.debug("query : {}", sql);

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        validateSingleResult(results);
        return results.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);

            return getResults(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new DataAccessException(e);
        }
    }

    private PreparedStatement prepareStatement(Connection connection, String sql, Object... args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int parameterIndex = 0; parameterIndex < args.length; ++parameterIndex) {
            preparedStatement.setObject(parameterIndex + 1, args[parameterIndex]);
        }
        return preparedStatement;
    }

    private <T> List<T> getResults(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 0개입니다.");
        }

        if (results.size() > 1) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 2개 이상입니다.");
        }
    }
}
