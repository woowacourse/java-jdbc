package org.springframework.jdbc.core;

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
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final String QUERY_FORMAT = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug(QUERY_FORMAT, sql);

            bindStatementWithArgs(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            bindStatementWithArgs(args, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug(QUERY_FORMAT, sql);

            if (resultSet.next()) {
                T result = rowMapper.mapRow(resultSet, resultSet.getRow());
                resultSet.close();

                return Optional.ofNullable(result);
            }

            resultSet.close();

            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void bindStatementWithArgs(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            preparedStatement.setObject(i, args[i - 1]);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            log.debug(QUERY_FORMAT, sql);

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(
                        rowMapper.mapRow(resultSet, resultSet.getRow())
                );
            }

            return results;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}
