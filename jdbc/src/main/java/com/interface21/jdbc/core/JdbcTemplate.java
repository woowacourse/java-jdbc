package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void update(Connection conn, String sql, Object... values) {
        try (PreparedStatement pstmt = createPstmt(conn, sql, values)) {
            pstmt.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollbackIfManualCommit(dataSource);
            throw new DataAccessException(e);
        }
    }

    public void update(String sql, Object... values) {
        runWithConnection((conn) -> update(conn, sql, values));
    }

    private <T> T queryOne(Connection conn, String sql, ResultExtractor<T> re, Object... values) {
        ResultSet rs = null;
        try (PreparedStatement pstmt = createPstmt(conn, sql, values)) {
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            if (rs.next()) return re.extract(rs);
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollbackIfManualCommit(dataSource);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> T queryOne(String sql, ResultExtractor<T> re, Object... values) {
        return getWithConnection((conn) -> queryOne(conn, sql, re, values));
    }

    private <T> List<T> queryMany(Connection conn, String sql, ResultExtractor<T> re, Object... values) {
        ResultSet rs = null;
        try (PreparedStatement pstmt = createPstmt(conn, sql, values)) {
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (rs.next()) results.add(re.extract(rs));
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollbackIfManualCommit(dataSource);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> queryMany(String sql, ResultExtractor<T> re, Object... values) {
        return getWithConnection((conn) -> queryMany(conn, sql, re, values));
    }

    private void runWithConnection(Consumer<Connection> consumer) {
        if (isTransactionExist(dataSource)) {
            Connection conn = getTransaction(dataSource).getConnection();
            consumer.accept(conn);
        } else {
            Connection conn = DataSourceUtils.createConnection(dataSource);
            consumer.accept(conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <T> T getWithConnection(Function<Connection, T> function) {
        if (isTransactionExist(dataSource)) {
            Connection conn = getTransaction(dataSource).getConnection();
            return function.apply(conn);
        } else {
            Connection conn = DataSourceUtils.createConnection(dataSource);
            T result = function.apply(conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
            return result;
        }
    }

    private static PreparedStatement createPstmt(Connection conn, String sql, Object... values) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }
        return pstmt;
    }

    private static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }

    private static void rollbackIfManualCommit(DataSource dataSource) {
        if (isTransactionExist(dataSource)) {
            Transaction transaction = getTransaction(dataSource);
            transaction.rollback();
        }
    }

    private static boolean isTransactionExist(DataSource dataSource) {
        TransactionManager transactionManager = TransactionManagerHolder.get(dataSource);
        if (transactionManager == null) return false;
        return transactionManager.getTransaction() != null;
    }

    private static Transaction getTransaction(DataSource dataSource) {
        return TransactionManagerHolder.get(dataSource).getTransaction();
    }
}
