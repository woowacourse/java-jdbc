package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int SQL_PARAMETER_INDEX_OFFSET = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeQuery(String sql, Object... params) {
        execute(sql, params, PreparedStatement::executeUpdate);
    }

    public void executeQuery(Connection connection, String sql, Object... params) {
        execute(connection, sql, params, PreparedStatement::executeUpdate);
    }

    public <T> Optional<T> executeQueryForObject(String sql, ObjectMaker<T> maker, Object... params) {
        return execute(sql, params, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            T object = maker.make(resultSet);
            if (resultSet.next()) {
                throw new IllegalArgumentException("결과가 두개 이상 존재합니다.");
            }

            return Optional.of(object);
        });
    }

    public <T> List<T> executeQueryForObjects(String sql, ObjectMaker<T> maker, Object... params) {
        return execute(sql, params, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                T object = maker.make(resultSet);
                objects.add(object);
            }

            return objects;
        });
    }

    private <T> T execute(String sql, Object[] params, PreparedStatementExecutor<T> executor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setParams(preparedStatement, params);
            log.debug("query : {}", sql);

            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다.", e);
        }
    }

    private <T> T execute(Connection connection, String sql, Object[] params, PreparedStatementExecutor<T> executor) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setParams(preparedStatement, params);
            log.debug("query : {}", sql);

            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다.", e);
        }
    }


    private void setParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            preparedStatement.setObject(index + SQL_PARAMETER_INDEX_OFFSET, params[index]);
        }
    }

    @FunctionalInterface
    public interface PreparedStatementExecutor<T> {
        T execute(PreparedStatement preparedStatement) throws SQLException;
    }
}
