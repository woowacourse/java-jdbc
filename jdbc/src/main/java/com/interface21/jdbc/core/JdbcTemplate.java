package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataAccessWrapper;
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

    private final DataAccessWrapper dataAccessWrapper;
    private final PreparedStatementResolver preparedStatementResolver;

    public JdbcTemplate(DataAccessWrapper dataAccessWrapper, PreparedStatementResolver preparedStatementResolver) {
        this.dataAccessWrapper = dataAccessWrapper;
        this.preparedStatementResolver = preparedStatementResolver;
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = query(connection, sql, rowMapper, parameters);
        validateResultsLength(results);
        return results.getFirst();
    }

    private void validateResultsLength(List<?> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("결과가 없습니다.");
        }

        if (results.size() >= 2) {
            log.debug("results : {} ", results);
            throw new DataAccessException("결과가 2개 이상입니다.");
        }
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... parameters) {
        return dataAccessWrapper.apply(
                connection,
                sql,
                (pstmt) -> makeQueryResultForList(rowMapper, parameters, pstmt)
        );
    }

    private <T> List<T> makeQueryResultForList(RowMapper<T> rowMapper, Object[] parameters, PreparedStatement pstmt)
            throws SQLException {
        PreparedStatement resolvedStatement = preparedStatementResolver.resolve(pstmt, parameters);
        ResultSet resultSet = resolvedStatement.executeQuery();
        return resolveQueryResult(resultSet, rowMapper);
    }

    private <T> List<T> resolveQueryResult(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    public int queryForUpdate(Connection connection, String sql, Object... parameters) {
        return dataAccessWrapper.apply(connection, sql, (pstmt) -> executeUpdateQuery(parameters, pstmt));
    }

    private int executeUpdateQuery(Object[] parameters, PreparedStatement pstmt) throws SQLException {
        PreparedStatement resolvedStatement = preparedStatementResolver.resolve(pstmt, parameters);
        return resolvedStatement.executeUpdate();
    }
}
