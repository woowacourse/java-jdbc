package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public int queryAndGetUpdateRowsCount(String sql, Object... parameters) {
        return executeQueryExecutor(PreparedStatement::executeUpdate, sql, parameters);
    }

    private <T> T executeQueryExecutor(QueryExecutor<T> queryExecutor, String sql, Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            log.debug("query : {}", sql);
            queryExecutor.setParameters(preparedStatement, parameters);
            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryAndGetResults(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        return executeQueryExecutor(sql, resultSetParser, new ListResultGenerator<>(), parameters);
    }

    private <T, R> R executeQueryExecutor(
            String sql,
            ResultSetParser<T> resultSetParser,
            ResultGenerator<T, R> rResultGenerator,
            Object... parameters
    ) {
        return executeQueryExecutor((preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return rResultGenerator.generate(resultSetParser, resultSet);
        }, sql, parameters);
    }

    public <T> T queryAndGetResult(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        return executeQueryExecutor(sql, resultSetParser, new SingleResultGenerator<>(), parameters);
    }
}
