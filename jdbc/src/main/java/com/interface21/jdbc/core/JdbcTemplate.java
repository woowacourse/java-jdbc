package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final H2SQLExceptionTranslator exceptionTranslator;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exceptionTranslator = new H2SQLExceptionTranslator();
    }

    public <T> T queryOne(String sql, ResultSetCallBack<T> callBack, Object... args) {
        debugQuery(sql);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var pstmt = conn.prepareStatement(sql)) {
            return executeQueryOne(pstmt, callBack, args);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        } finally {
            tryRelease(conn);
        }
    }

    private <T> T executeQueryOne(PreparedStatement pstmt, ResultSetCallBack<T> callBack, Object... args) throws SQLException {
        setArg(pstmt, args);

        try (ResultSet rs = pstmt.executeQuery()) {
            return createResult(rs, callBack);
        }
    }

    private <T> T createResult(ResultSet rs, ResultSetCallBack<T> callBack) throws SQLException {
        T result = null;
        if (rs.next()) {
            result = callBack.callback(rs);
        }

        return result;
    }

    public <T> List<T> query(String sql, ResultSetCallBack<T> callBack, Object... args) {
        debugQuery(sql);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var pstmt = conn.prepareStatement(sql)) {
            return executeQuery(pstmt, callBack, args);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        } finally {
            tryRelease(conn);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, ResultSetCallBack<T> callBack, Object... args) throws SQLException {
        setArg(pstmt, args);

        try (ResultSet rs = pstmt.executeQuery();) {
            return createResults(rs, callBack);
        }
    }

    private <T> List<T> createResults(ResultSet rs, ResultSetCallBack<T> callBack) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(callBack.callback(rs));
        }

        return results;
    }

    private void setArg(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    public void update(String sql, PreparedStatementCallBack callBack) {
        debugQuery(sql);

        var conn = DataSourceUtils.getConnection(dataSource);
        try (var pstmt = conn.prepareStatement(sql)) {
            executeUpdate(callBack, pstmt);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        } finally {
            tryRelease(conn);
        }
    }

    private void executeUpdate(PreparedStatementCallBack callBack, PreparedStatement pstmt) throws SQLException {
        callBack.callback(pstmt);
        pstmt.executeUpdate();
    }

    private void debugQuery(String sql) {
        log.debug("query : {}", sql);
    }

    private void tryRelease(Connection conn) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }

        DataSourceUtils.releaseConnection(conn, dataSource);
    }
}
