package com.interface21.jdbc.core;

import com.interface21.jdbc.CustomizedDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            Connection connection,
            String sql,
            PreparedStatementSetter setter,
            PreparedStatementCallback<T> action
    ) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.setValues(ps);
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public <T> T execute(
            String sql,
            PreparedStatementSetter setter,
            PreparedStatementCallback<T> action
    ) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, sql, setter, action);
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public void update(
            Connection conn,
            String sql,
            PreparedStatementSetter setter
    ) {
        execute(conn, sql, setter, preparedStatement -> {
            preparedStatement.executeUpdate();
            return Optional.empty();
        });
    }

    public void update(
            String sql,
            PreparedStatementSetter setter
    ) {
        execute(sql, setter, preparedStatement -> {
            preparedStatement.executeUpdate();
            return Optional.empty();
        });
    }

    public <T> Optional<T> queryForObject(
            Connection conn,
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(conn, sql, setter, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            T wantToFind = rowMapper.mapRow(resultSet);

            if (resultSet.next()) {
                throw new CustomizedDataAccessException(
                        sql,
                        new IllegalArgumentException("[ERROR] too many rows (Expected 1 but found 2 or more)")
                );
            }
            return Optional.of(wantToFind);
        });
    }

    public <T> Optional<T> queryForObject(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(sql, setter, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            T wantToFind = rowMapper.mapRow(resultSet);

            if (resultSet.next()) {
                throw new CustomizedDataAccessException(
                        sql,
                        new IllegalArgumentException("[ERROR] too many rows (Expected 1 but found 2 or more)")
                );
            }
            return Optional.of(wantToFind);
        });
    }

    public <T> List<T> queryForObjects(
            Connection conn,
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(conn, sql, setter, preparedStatement -> {
            List<T> results = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }

    public <T> List<T> queryForObjects(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(sql, setter, preparedStatement -> {
            List<T> results = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }
}
