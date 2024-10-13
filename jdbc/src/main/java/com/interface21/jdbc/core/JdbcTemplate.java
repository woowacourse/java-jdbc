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

    public void update(String sql, Connection connection, Object... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        StatementParamSetter.setParams(preparedStatement, params);
        preparedStatement.executeUpdate();
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
        Executor<ResultSet, T> resultMapper = (resultSet) -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        };
        return connect(creator, preparedStatement -> getResult(preparedStatement, resultMapper));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        Executor<ResultSet, List<T>> resultMapper = resultSet -> {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        };

        return connect(connection -> connection.prepareStatement(sql),
                preparedStatement -> getResult(preparedStatement, resultMapper));
    }

    private <R> R connect(PreparedStatementCreator creator, Executor<PreparedStatement, R> executor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = creator.createPreparedStatement(connection)) {
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <R> R getResult(PreparedStatement preparedStatement, Executor<ResultSet, R> executor) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return executor.execute(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private interface Executor<T, R> {
        R execute(T t) throws SQLException;
    }
}
