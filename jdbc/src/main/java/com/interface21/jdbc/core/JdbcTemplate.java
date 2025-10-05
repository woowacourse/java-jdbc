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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            String sql,
            PreparedStatementSetter setter
    ) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setter.setValues(preparedStatement);
            logQuery(sql);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public <T> Optional<T> queryForObject(
        String sql,
        PreparedStatementSetter setter,
        RowMapper<T> rowMapper
    ) {
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setter.setValues(preparedStatement);
            logQuery(sql);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public <T> List<T> queryForObjects(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        List<T> objects = new ArrayList<>();
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setter.setValues(preparedStatement);
            logQuery(sql);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    objects.add(rowMapper.mapRow(resultSet));
                }
            }

            return objects;
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    private void logQuery(String sql) {
        log.info("query : {}", sql);
    }
}
