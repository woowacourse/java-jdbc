package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void update(String sql, Object... params) {
        update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            StatementParamSetter.setParams(ps, params);
            return ps;
        });
    }

    public <R> R connect(PreparedStatementCreator creator, Executor<PreparedStatement, R> executor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = creator.createPreparedStatement(connection)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(PreparedStatementCreator creator) {
        connect(creator, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            StatementParamSetter.setParams(ps, params);
            return ps;
        }, rowMapper);
    }

    public <T> T queryForObject(PreparedStatementCreator creator, RowMapper<T> rowMapper) {
        return connect(creator, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery(); // todo close resultSet
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return connect(connection -> connection.prepareStatement(sql),
                preparedStatement -> {
                    ResultSet resultSet = preparedStatement.executeQuery(); // todo close resultSet
                    List<T> results = new ArrayList<>();
                    while (resultSet.next()) {
                        results.add(rowMapper.mapRow(resultSet));
                    }
                    return results;
                });
    }

    private interface Executor<T, R> {
        R execute(T t) throws SQLException;
    }
}
