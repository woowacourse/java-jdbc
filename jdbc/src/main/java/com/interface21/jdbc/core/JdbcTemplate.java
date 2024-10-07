package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void executeQuery(String sql, List<Object> paramList) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(paramList, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다.", e);
        }
    }

    public <T> T executeQueryForObject(String sql, List<Object> paramList, ObjectMaker<T> maker) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setParams(paramList, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (resultSet.next()) {
                return maker.make(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다.", e);
        }
    }

    public <T> List<T> executeQueryForObjects(String sql, List<Object> paramList, ObjectMaker<T> maker) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setParams(paramList, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            List<T> objects = new ArrayList<>();
            if (resultSet.next()) {
                T object = maker.make(resultSet);
                objects.add(object);
            }
            return objects;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 오류가 발생했습니다.", e);
        }
    }

    private void setParams(List<Object> paramList, PreparedStatement preparedStatement) throws SQLException {
        for (int index = 1; index <= paramList.size(); index++) {
            preparedStatement.setObject(index, paramList.get(index - 1));
        }
    }
}
