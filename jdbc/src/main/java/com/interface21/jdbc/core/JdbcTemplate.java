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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... objects) {
        execute(sql, preparedStatement -> {
            ParameterSetter.setParameter(objects, preparedStatement);
            return preparedStatement.executeUpdate();
        });
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
        return execute(sql, preparedStatement -> {
            ParameterSetter.setParameter(objects, preparedStatement);
            return getQueryResult(rowMapper, preparedStatement);
        });
    }

    private <T> T execute(String sql, Executor<T> executor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet rs = preparedStatement.executeQuery()) {
            return getQueryResult(rowMapper, rs);
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
