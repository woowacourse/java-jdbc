package com.interface21.jdbc.core;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final SqlExecutor sqlExecutor;

    public JdbcTemplate(DataSource dataSource) {
        this.sqlExecutor = new SqlExecutor(dataSource);
    }

    public void update(String sql, Object... params) {
        sqlExecutor.execute(sql, preparedStatement -> {
            PreparedStatementParameterBinder.bindStatementParameters(preparedStatement, params);
            preparedStatement.executeUpdate();
            return null;
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return sqlExecutor.execute(sql, preparedStatement -> {
            PreparedStatementParameterBinder.bindStatementParameters(preparedStatement, params);
            ResultSet resultSet = preparedStatement.executeQuery();
            return getListFromResultSet(resultSet, rowMapper);
        });
    }

    private <T> List<T> getListFromResultSet(ResultSet resultSet, RowMapper<T> rowMapper)
            throws SQLException {
        List<T> queriedData = new ArrayList<>();
        while (resultSet.next()) {
            queriedData.add(rowMapper.map(resultSet));
        }
        return queriedData;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> queriedData = query(sql, rowMapper, params);
        if (queriedData.size() < 1) {
            throw new EmptyResultDataAccessException();
        }
        if (queriedData.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, queriedData.size());
        }
        return queriedData.get(0);
    }
}
