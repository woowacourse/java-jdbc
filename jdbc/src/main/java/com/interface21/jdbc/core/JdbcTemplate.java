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
    private final PreparedStatementResolver preparedStatementResolver;

    public JdbcTemplate(final DataSource dataSource, PreparedStatementResolver preparedStatementResolver) {
        this.dataSource = dataSource;
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            PreparedStatement resolvedStatement = preparedStatementResolver.resolve(pstmt, parameters);
            ResultSet resultSet = resolvedStatement.executeQuery();
            return makeQueryResult(resultSet, rowMapper);
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        }
    }

    private <T> List<T> makeQueryResult(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    public int queryForUpdate(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            PreparedStatement resolvedStatement = preparedStatementResolver.resolve(pstmt, parameters);
            return resolvedStatement.executeUpdate();
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        }
    }
}
