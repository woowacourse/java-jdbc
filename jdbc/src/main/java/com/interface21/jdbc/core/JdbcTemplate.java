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

    public void update(PreparedStatementCreator creator) {
        try (PreparedStatement preparedStatement = getPreparedStatement(creator);) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            StatementParamSetter.setParams(ps, params);
            return ps;
        }, rowMapper);
    }

    public <T> T queryForObject(PreparedStatementCreator creator, RowMapper<T> rowMapper) {
        try (PreparedStatement preparedStatement = getPreparedStatement(creator);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (PreparedStatement preparedStatement = getPreparedStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql) throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection.prepareStatement(sql);
    }

    private PreparedStatement getPreparedStatement(PreparedStatementCreator creator) throws SQLException {
        Connection connection = dataSource.getConnection();
        return creator.createPreparedStatement(connection);
    }
}
