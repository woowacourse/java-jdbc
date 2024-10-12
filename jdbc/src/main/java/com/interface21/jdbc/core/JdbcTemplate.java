package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);


    public JdbcTemplate() {
    }

    public void execute(Connection connection, String sql, Object... args) {
        executeStatement(connection, sql, PreparedStatement::execute, new ArgumentPreparedStatementSetter(args));
    }

    public int update(Connection connection, String sql, Object... args) {
        return update(connection, sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter) {
        return executeStatement(connection, sql, PreparedStatement::executeUpdate, preparedStatementSetter);
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(connection, sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> T queryForObject(
            Connection connection,
            String sql,
            RowMapper<T> rowMapper,
            PreparedStatementSetter preparedStatementSetter) {
        List<T> results = query(connection, sql, rowMapper, preparedStatementSetter);
        validateSingleResult(results);
        return results.getFirst();
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        return query(connection, sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(
            Connection connection,
            String sql,
            RowMapper<T> rowMapper,
            PreparedStatementSetter preparedStatementSetter) {
        StatementExecutor<List<T>> statementExecutor = preparedStatement -> mapRows(preparedStatement, rowMapper);
        return executeStatement(connection, sql, statementExecutor, preparedStatementSetter);
    }

    private <T> T executeStatement(
            Connection connection,
            String sql,
            StatementExecutor<T> statementExecutor,
            PreparedStatementSetter preparedStatementSetter) {
        try (PreparedStatement preparedStatement = prepareStatement(connection, sql, preparedStatementSetter)) {
            log.debug("query : {}", sql);

            return statementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new DataAccessException(e);
        }
    }

    private PreparedStatement prepareStatement(
            Connection connection, String sql, PreparedStatementSetter preparedStatementSetter) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatementSetter.setValues(preparedStatement);
        return preparedStatement;
    }

    private <T> List<T> mapRows(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 0개입니다.");
        }

        if (results.size() > 1) {
            throw new DataAccessException("쿼리 실행 결과가 1개이기를 기대했지만, 2개 이상입니다.");
        }
    }
}
