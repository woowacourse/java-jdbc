package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.PreparedStatementExecuteException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::execute, args);
        log.debug("query : {}", sql);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeForObject(sql, rowMapper, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeForObjects(sql, rowMapper, args);
    }

    private <T> List<T> executeForObjects(String sql, RowMapper<T> rowMapper, Object[] args) {
        return execute(sql, preparedStatement -> getObjects(preparedStatement, rowMapper), args);
    }

    private <T> T executeForObject(String sql, RowMapper<T> rowMapper, Object[] args) {
        return execute(sql, preparedStatement -> getObject(preparedStatement, rowMapper), args);
    }

    private <T> T getObject(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return rowMapper.map(resultSet);
        } else {
            throw new DataAccessException();
        }
    }

    private <T> List<T> getObjects(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.map(resultSet));
        }
        return result;
    }

    private <T> T execute(
            String sql,
            PreparedStatementExecutor<T> preparedStatementExecutor,
            Object... args
    ) {
        try (var conn = dataSource.getConnection();
             var pstmt = conn.prepareStatement(sql);

        ) {
            setParams(pstmt, args);

            return preparedStatementExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new PreparedStatementExecuteException();
        }
    }

    private void setParams(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
