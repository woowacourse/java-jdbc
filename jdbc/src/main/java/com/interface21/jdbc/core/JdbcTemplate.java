package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_RESULT_INDEX = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = createPreparedStatement(connection, sql, parameters)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error("쿼리 실행에 실패했습니다: {}", sql, e);
            throw new RuntimeException("쿼리 실행에 실패했습니다.", e);
        }
    }

    public <T> List<T> fetchResults(String sql, Function<ResultSet, T> resultMapper, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = createPreparedStatement(connection, sql, parameters);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            return mapResultSet(resultSet, resultMapper);

        } catch (SQLException e) {
            log.error("쿼리 실행에 실패했습니다: {}", sql, e);
            throw new RuntimeException("쿼리 실행에 실패했습니다.", e);
        }
    }

    public <T> T fetchResult(String sql, Function<ResultSet, T> resultMapper, Object... parameters) {
        List<T> results = fetchResults(sql, resultMapper, parameters);
        return results.get(FIRST_RESULT_INDEX);
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Object... parameters)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        log.debug("실행 쿼리: {}", sql);
        setParameters(preparedStatement, parameters);
        return preparedStatement;
    }

    private void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> List<T> mapResultSet(ResultSet resultSet, Function<ResultSet, T> resultMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(resultMapper.apply(resultSet));
        }
        return results;
    }
}
