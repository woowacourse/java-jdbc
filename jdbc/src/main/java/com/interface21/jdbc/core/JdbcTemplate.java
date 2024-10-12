package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    public JdbcTemplate() {
    }

    public void update(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new DataAccessException("update 메서드를 실행하는 과정에서 예상치 못한 예외가 발생했습니다.", sqlException);
        }
    }

    public <T> T queryForObject(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return executeQuery(connection, sql, preparedStatementSetter, rowMapper).getFirst();
    }

    public <T> List<T> queryForList(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return executeQuery(connection, sql, preparedStatementSetter, rowMapper);
    }

    private <T> List<T> executeQuery(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException sqlException) {
            throw new DataAccessException("query 메서드를 실행하는 과정에서 예상치 못한 예외가 발생했습니다.", sqlException);
        }
    }
}
