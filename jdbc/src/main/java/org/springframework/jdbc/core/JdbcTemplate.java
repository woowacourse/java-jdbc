package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.datasource.ConnectionManager;
import org.springframework.transaction.support.TransactionManager;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        return executeQuery(queryForObjectCallback(rowMapper), sql, pstmtSetter);
    }

    private <T> ResultSetCallback<T> queryForObjectCallback(RowMapper<T> rowMapper) {
        return rs -> {
            if (!rs.next()) {
                throw new EmptyResultDataAccessException();
            }
            T result = rowMapper.mapRow(rs);
            if (rs.next()) {
                throw new IncorrectResultSizeDataAccessException();
            }
            return result;
        };
    }

    private <T> T executeQuery(ResultSetCallback<T> callback, String sql, PreparedStatementSetter setter) {
        Connection conn = ConnectionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementSetter(conn.prepareStatement(sql), setter);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            return callback.call(rs);
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                ConnectionManager.releaseConnection();
            }
        }
    }

    private PreparedStatement applyPreparedStatementSetter(PreparedStatement pstmt, PreparedStatementSetter pstmtSetter)
        throws SQLException {
        pstmtSetter.apply(pstmt);
        return pstmt;
    }

    private void logQuery(String sql) {
        log.debug("query : {}", sql);
    }

    private void logException(SQLException e) {
        log.error(e.getMessage(), e);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(queryForObjectCallback(rowMapper), sql, parameters);
    }

    private <T> T executeQuery(ResultSetCallback<T> callback, String sql, Object[] objects) {
        Connection conn = ConnectionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementParameters(conn.prepareStatement(sql), objects);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            return callback.call(rs);
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                ConnectionManager.releaseConnection();
            }
        }
    }

    private PreparedStatement applyPreparedStatementParameters(PreparedStatement pstmt, Object[] parameters)
        throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
        return pstmt;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeQuery(queryCallback(rowMapper), sql);
    }

    private <T> ResultSetCallback<List<T>> queryCallback(RowMapper<T> rowMapper) {
        return rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        };
    }

    private <T> T executeQuery(ResultSetCallback<T> callback, String sql) {
        Connection conn = ConnectionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            return callback.call(rs);
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                ConnectionManager.releaseConnection();
            }
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        return executeQuery(queryCallback(rowMapper), sql, pstmtSetter);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(queryCallback(rowMapper), sql, parameters);
    }

    public void update(String sql, PreparedStatementSetter pstmtSetter) {
        Connection conn = ConnectionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementSetter(conn.prepareStatement(sql), pstmtSetter)
        ) {
            pstmt.executeUpdate();
            logQuery(sql);
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                ConnectionManager.releaseConnection();
            }
        }
    }

    public void update(String sql, Object... parameters) {
        Connection conn = ConnectionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementParameters(conn.prepareStatement(sql), parameters)
        ) {
            pstmt.executeUpdate();
            logQuery(sql);
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                ConnectionManager.releaseConnection();
            }
        }
    }

    @FunctionalInterface
    public interface PreparedStatementSetter {

        void apply(PreparedStatement pstmt) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetCallback<T> {

        T call(ResultSet rs) throws SQLException;
    }
}
