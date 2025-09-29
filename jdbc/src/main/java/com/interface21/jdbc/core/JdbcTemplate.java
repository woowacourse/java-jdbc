package com.interface21.jdbc.core;

import com.interface21.jdbc.CustomizedDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            String sql,
            Object... args
    ) {

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParameters(preparedStatement, args);
            logQuery(sql);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public <T>T queryForObject(
        String sql,
        RowMapper<T> rowMapper,
        Object... args
    ) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParameters(preparedStatement, args);
            logQuery(sql);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }

                throw new NoSuchElementException("[ERROR] no such user" + sql);
            }
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public <T> List<T> queryForObjects(
            String sql,
            RowMapper<T> rowMapper,
            Object... args
    ) {
        List<T> objects = new ArrayList<>();
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParameters(preparedStatement, args);
            logQuery(sql);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    objects.add(rowMapper.mapRow(resultSet));
                }
            }

            if (objects.isEmpty()) {
                throw new NoSuchElementException("[ERROR] no such user" + sql);
            }

            return objects;
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    private void setParameters(PreparedStatement parameters, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            parameters.setObject(i + 1, args[i]);
        }
    }

    private void logQuery(String sql) {
        log.info("query : {}", sql);
    }
}
