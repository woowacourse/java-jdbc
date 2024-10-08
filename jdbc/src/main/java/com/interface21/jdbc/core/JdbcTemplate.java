package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public int queryAndGetUpdateRowsCount(String sql, Object... parameters) {
        return executeQueryExecutor(PreparedStatement::executeUpdate, sql, parameters);
    }

    private <T> T executeQueryExecutor(QueryExecutor<T> queryExecutor, String sql, Object... parameters) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            queryExecutor.setParameters(preparedStatement, parameters);
            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryAndGetResults(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        return executeQueryExecutor((preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return parseResults(resultSetParser, resultSet);
        }, sql, parameters);
    }

    private <T> List<T> parseResults(ResultSetParser<T> resultSetParser, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(resultSetParser.parse(resultSet));
        }
        return results;
    }

    public <T> T queryAndGetResult(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        return executeQueryExecutor((preparedStatement) -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            return parseResult(resultSetParser, resultSet);
        }, sql, parameters);
    }

    private <T> T parseResult(ResultSetParser<T> resultSetParser, ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("행이 하나도 조회되지 않았습니다.");
        }
        if (!resultSet.isLast()) {
            throw new DataAccessException("여러개의 행이 조회되었습니다.");
        }
        return resultSetParser.parse(resultSet);
    }
}
