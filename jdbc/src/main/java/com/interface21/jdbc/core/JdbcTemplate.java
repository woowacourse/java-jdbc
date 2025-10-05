package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
            rollbackIfManualCommit();
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
            rollbackIfManualCommit();
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
            rollbackIfManualCommit();
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> queryMany(String sql, ResultExtractor<T> re, Object... values) {
        return getWithConnection((conn) -> queryMany(conn, sql, re, values));
    }

    private void runWithConnection(Consumer<Connection> consumer) {
        Transaction transaction = TransactionHolder.getTransaction();
        if (transaction != null && transaction.isStarted()) {
            Connection conn = transaction.getConnection();
            consumer.accept(conn);
        } else {
            try (Connection conn = dataSource.getConnection()) {
                consumer.accept(conn);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }
    }

    private <T> T getWithConnection(Function<Connection, T> function) {
        Transaction transaction = TransactionHolder.getTransaction();
        if (transaction != null && transaction.isStarted()) {
            Connection conn = transaction.getConnection();
            return function.apply(conn);
        } else {
            try (Connection conn = dataSource.getConnection()) {
                return function.apply(conn);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
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

    private static void rollbackIfManualCommit() {
        Transaction transaction = TransactionHolder.getTransaction();
        if (transaction.isStarted()) transaction.rollback();
    }
}
