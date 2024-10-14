package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.DataQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementSetter preparedStatementSetter;
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.preparedStatementSetter = new PreparedStatementSetter();
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return Optional.ofNullable(executeQuery(connection, sql,
                resultSet -> mapSingleRow(rowMapper, resultSet),
                values)
        );
    }

    private <T> T mapSingleRow(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.mapRow(resultSet);
        }
        return null;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return executeQuery(connection, sql,
                resultSet -> mapRows(rowMapper, resultSet),
                values);
    }

    private <T> List<T> mapRows(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private <T> T executeQuery(Connection connection, String sql, ResultSetExtractor<T> resultSetExtractor,
                               Object... values) {
        return execute(connection, sql, (preparedStatement -> {
            preparedStatementSetter.setValues(values, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);
            return resultSetExtractor.extractData(resultSet);
        }));
    }

    public void update(String sql, Object... values) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        execute(connection, sql, (preparedStatement -> {
            log.debug("query : {}", sql);
            preparedStatementSetter.setValues(values, preparedStatement);
            return preparedStatement.executeUpdate();
        }));
    }

    private <T> T execute(Connection connection, String sql, JdbcTemplateExecutor<T> jdbcTemplateExecutor) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return jdbcTemplateExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataQueryException(e.getMessage(), e);
        }
    }
}
