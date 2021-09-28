package nextstep.jdbc.core;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.JdbcTemaplateQueryExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(String sql, StatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stm = conn.prepareStatement(sql)) {
            return callback.executeQuery(stm);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcTemaplateQueryExecutionException(e);
        }
    }

    public void update(String sql, Object... args) {
        log.debug("update() : {}", sql);

        StatementCallback<Integer> statementCallback = (preparedStatement) -> {
            setArgs(preparedStatement, args);
            return preparedStatement.executeUpdate();
        };

        execute(sql, statementCallback);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("queryForObject() : {}", sql);

        List<T> results = query(sql, rowMapper, args);
        if (results.size() < 1) {
            throw new DataAccessException();
        }
        return results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new ResultListExtractorFromRowMapper<>(rowMapper), args);
    }

    private <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, Object... args) {
        log.debug("query() : {}", sql);

        StatementCallback<T> statementCallback = (preparedStatement) -> {
            setArgs(preparedStatement, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSetExtractor.extractData(resultSet);
        };
        return execute(sql, statementCallback);
    }

    private void setArgs(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
