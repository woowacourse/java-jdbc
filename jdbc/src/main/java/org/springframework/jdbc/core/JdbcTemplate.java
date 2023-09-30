package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.PreparedStatementExecuteException;

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
        List<T> result = executeForQuery(sql, rowMapper, args);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeForQuery(sql, rowMapper, args);
    }

    private <T> List<T> executeForQuery(String sql, RowMapper<T> rowMapper, Object[] args) {
        return execute(sql, preparedStatement -> getObjects(preparedStatement, rowMapper), args);
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
