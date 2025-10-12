package com.interface21.jdbc.core;

import com.interface21.dao.RowMapper;
import com.interface21.jdbc.datasource.LocalTransactionManager;
import com.interface21.jdbc.exception.JdbcFailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try {
            Connection connection = LocalTransactionManager.getConnection(dataSource);
            PreparedStatement ps = connection.prepareStatement(sql);
            return callback.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcFailException(e.getSQLState(), e.getMessage());
        }
    }

    public void update(String sql, Object... objects) {
        execute(sql, preparedStatement -> {
            setParameters(preparedStatement, objects);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... objects) {
        return execute(sql, preparedStatement -> {
            setParameters(preparedStatement, objects);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
                }
                return results;
            }
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        List<T> results = queryForObjects(sql, rowMapper, objects);
        if (results.isEmpty()) {
            throw new JdbcFailException("유효한 데이터를 찾는데 실패하였습니다.");
        }
        if (results.size() > 1) {
            throw new JdbcFailException("결과가 하나 이상입니다.");
        }
        return results.getFirst();
    }

    private void setParameters(PreparedStatement preparedStatement, Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
    }
}


