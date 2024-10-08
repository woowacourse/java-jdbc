package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataAccessWrapper;
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

    private final DataAccessWrapper dataAccessWrapper;
    private final PreparedStatementResolver preparedStatementResolver;

    public JdbcTemplate(final DataSource dataSource, PreparedStatementResolver preparedStatementResolver) {
        this.dataAccessWrapper = new DataAccessWrapper(dataSource);
        this.preparedStatementResolver = preparedStatementResolver;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return dataAccessWrapper.apply((connection, pstmt) -> makeQueryResultForList(rowMapper, parameters, pstmt), sql);
    }

    private <T> List<T> makeQueryResultForList(RowMapper<T> rowMapper, Object[] parameters, PreparedStatement pstmt) throws SQLException {
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

    public int queryForUpdate(String sql, Object... parameters) {
        return dataAccessWrapper.apply((connection, pstmt) -> executeUpdateQuery(parameters, pstmt), sql);
    }

    private int executeUpdateQuery(Object[] parameters, PreparedStatement pstmt) throws SQLException {
        PreparedStatement resolvedStatement = preparedStatementResolver.resolve(pstmt, parameters);
        return resolvedStatement.executeUpdate();
    }
}
