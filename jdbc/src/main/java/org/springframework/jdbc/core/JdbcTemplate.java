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
import org.springframework.transaction.support.TransactionManager;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pstmtFunc, RowMapper<T> rsMapper) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementSetter(conn.prepareStatement(sql), pstmtFunc);
            ResultSet rs = pstmt.executeQuery()
        ) {
            if (!rs.next()) {
                throw new EmptyResultDataAccessException();
            }
            T result = rsMapper.mapRow(rs);
            if (rs.next()) {
                throw new IncorrectResultSizeDataAccessException();
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    private void logQuery(String sql) {
        log.debug("query : {}", sql);
    }

    private void logException(SQLException e) {
        log.error(e.getMessage(), e);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rsMapper, Object... parameters) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementParameters(conn.prepareStatement(sql), parameters);
            ResultSet rs = pstmt.executeQuery()
        ) {
            if (!rs.next()) {
                throw new EmptyResultDataAccessException();
            }
            T result = rsMapper.mapRow(rs);
            if (rs.next()) {
                throw new IncorrectResultSizeDataAccessException();
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementSetter(conn.prepareStatement(sql), pstmtSetter);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementParameters(conn.prepareStatement(sql), parameters);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    public void update(String sql, PreparedStatementSetter pstmtSetter) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmtSetter.apply(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    public void update(String sql, Object... parameters) {
        logQuery(sql);
        Connection conn = TransactionManager.getConnection(dataSource);
        try (
            PreparedStatement pstmt = applyPreparedStatementParameters(conn.prepareStatement(sql), parameters)
        ) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (!TransactionManager.isTransactionEnable()) {
                TransactionManager.releaseConnection();
            }
        }
    }

    private PreparedStatement applyPreparedStatementSetter(PreparedStatement pstmt,
                                                           PreparedStatementSetter pstmtSetter)
        throws SQLException {
        pstmtSetter.apply(pstmt);
        return pstmt;
    }

    private PreparedStatement applyPreparedStatementParameters(PreparedStatement pstmt, Object[] parameters)
        throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
        return pstmt;
    }

    @FunctionalInterface
    public interface PreparedStatementSetter {

        void apply(PreparedStatement pstmt) throws SQLException;
    }
}
