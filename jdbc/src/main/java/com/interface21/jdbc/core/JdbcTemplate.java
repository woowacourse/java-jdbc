package com.interface21.jdbc.core;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

@Component
public class JdbcTemplate {

    @Inject
    private DataSource dataSource;

    private JdbcTemplate() {}

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public void update(String sql, Object ... args) {
        update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return getQueryResult(rowMapper, preparedStatement);
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ... args) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ... args) {
        List<T> query = query(sql, rowMapper, args);
        if (query.isEmpty()) {
            throw new DataAccessException("결과가 존재하지 않습니다");
        }
        if (query.size() > 1) {
            throw new DataAccessException("2개 이상의 결과가 조회되었습니다");
        }
        return query.getFirst();
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
