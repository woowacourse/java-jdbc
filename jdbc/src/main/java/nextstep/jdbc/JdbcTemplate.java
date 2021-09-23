package nextstep.jdbc;

import nextstep.exception.DataAccessException;
import nextstep.exception.connectioon.ConnectionAcquisitionFailureException;
import nextstep.exception.statement.StatementExecutionFailureException;
import nextstep.exception.statement.StatementInitializationFailureException;
import nextstep.jdbc.mapper.ObjectMapper;
import nextstep.jdbc.resultset.ResultSetExtractor;
import nextstep.jdbc.statement.StatementExecutionStrategy;
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

    public int execute(String sql, Object... args) {
        return context(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T executeForObject(String sql, ObjectMapper<T> objectMapper, Object... args) {
        StatementExecutionStrategy<T> singleObjectStrategy = pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(objectMapper);
            return resultSetExtractor.extractSingleObject(resultSet);
        };
        return context(sql, singleObjectStrategy, args);
    }

    public <T> List<T> executeForList(String sql, ObjectMapper<T> objectMapper, Object... args) {
        StatementExecutionStrategy<List<T>> listStrategy = pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(objectMapper);
            return resultSetExtractor.extractList(resultSet);
        };
        return context(sql, listStrategy, args);
    }

    private <T> T context(String sql, StatementExecutionStrategy<T> executionStrategy, Object... args) {
        try (
                final Connection conn = getConnection();
                final PreparedStatement pstmt = createPreparedStatement(conn, sql, args)
        ) {
            log.debug("query : {}, {}", sql, args);
            return applyStrategy(executionStrategy, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionAcquisitionFailureException(e.getMessage());
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, String sql, Object[] args) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            int i = 1;
            for (Object arg : args) {
                preparedStatement.setObject(i++, arg);
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new StatementInitializationFailureException(e.getMessage());
        }
    }

    private <T> T applyStrategy(StatementExecutionStrategy<T> executionStrategy, PreparedStatement pstmt) {
        try {
            return executionStrategy.apply(pstmt);
        } catch (SQLException e) {
            throw new StatementExecutionFailureException(e.getMessage());
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
