package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public int update(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            return statement.executeUpdate(sql);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sql);
            List<T> objects = new ArrayList<>();

            while (resultSet.next()) {
                objects.add(rowMapper.mapToObject(resultSet));
            }
            return objects;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(ps);
            ResultSet resultSet = ps.executeQuery();
            List<T> objects = new ArrayList<>();

            while (resultSet.next()) {
                objects.add(rowMapper.mapToObject(resultSet));
            }
            return objects;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Object[] objects = args.clone();

            for (int i = 1; i <= objects.length; i++) {
                ps.setObject(i, objects[i - 1]);
            }
            ResultSet resultSet = ps.executeQuery();
            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(rowMapper.mapToObject(resultSet));
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        List<T> results = query(sql, rowMapper);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new IllegalStateException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public <T> T queryForObejct(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        List<T> results = query(sql, preparedStatementSetter, rowMapper);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new IllegalStateException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public <T> T queryForObejct(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new IllegalStateException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            preparedStatementSetter.setValues(ps);
            return ps.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


}
