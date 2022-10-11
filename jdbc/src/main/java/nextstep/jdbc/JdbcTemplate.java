package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.SQLAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return queryList(sql, rowMapper);
    }

    private <T> T execute(String sql, ExecuteStrategy<T> strategy, Object... args) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);
            T result = strategy.execute(pstmt);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new SQLAccessException();
        }
    }

    private <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            putArguments(pstmt, args);

            List<T> result = extractResult(rowMapper, pstmt);
            transactionManager.commit(transactionStatus);
            return result.get(0);
        } catch (SQLException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new SQLAccessException();
        }
    }

    private <T> List<T> queryList(String sql, RowMapper<T> rowMapper) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            List<T> result = extractResult(rowMapper, pstmt);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
            throw new SQLAccessException();
        }
    }

    private <T> List<T> extractResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet resultSet = pstmt.executeQuery();
        List<T> result = new ArrayList<>();
        for (int i = 0; resultSet.next(); i++) {
            result.add(rowMapper.mapRow(resultSet, i));
        }
        return result;
    }

    private void putArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
