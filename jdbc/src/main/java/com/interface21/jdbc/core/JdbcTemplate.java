package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            validateParameterCount(objects, preparedStatement);
            setParameter(objects, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ...objects) {
        List<T> query = query(sql, rowMapper, objects);
        if (query.isEmpty()) {
            throw new DataAccessException("결과가 존재하지 않습니다");
        }
        if (query.size() > 1) {
            throw new DataAccessException("2개 이상의 결과가 조회되었습니다");
        }
        return query.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ...objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            validateParameterCount(objects, preparedStatement);
            setParameter(objects, preparedStatement);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return getQueryResult(rowMapper, rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private void validateParameterCount(Object[] objects, PreparedStatement preparedStatement) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        if (objects.length != parameterMetaData.getParameterCount()) {
            throw new DataAccessException("파라미터 값의 개수가 올바르지 않습니다");
        }
    }

    private void setParameter(Object[] objects, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }
}
