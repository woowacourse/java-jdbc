package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryOne(String sql, ResultSetCallBack<T> callBack, Object... args) {
        debugQuery(sql);

        try (var conn = dataSource.getConnection(); var pstmt = conn.prepareStatement(sql)) {
            return executeQueryOne(callBack, pstmt, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T executeQueryOne(ResultSetCallBack<T> callBack, PreparedStatement pstmt, Object... args) throws SQLException {
        setArg(args, pstmt);
        ResultSet rs = pstmt.executeQuery();

        T result = null;
        if (rs.next()) {
            result = callBack.callback(rs);
        }

        rs.close();
        return result;
    }

    public <T> List<T> query(String sql, ResultSetCallBack<T> callBack, Object... args) {
        debugQuery(sql);

        try (var conn = dataSource.getConnection(); var pstmt = conn.prepareStatement(sql)) {
            return executeQuery(callBack, pstmt, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> executeQuery(ResultSetCallBack<T> callBack, PreparedStatement pstmt, Object... args) throws SQLException {
        setArg(args, pstmt);
        ResultSet rs = pstmt.executeQuery();

        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(callBack.callback(rs));
        }

        rs.close();
        return results;
    }

    private void setArg(Object[] args, PreparedStatement pstmt) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    public void update(String sql, PreparedStatementCallBack callBack) {
        debugQuery(sql);

        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            executeUpdate(callBack, pstmt);
        } catch (SQLException ignored) {
        }
    }

    private void executeUpdate(PreparedStatementCallBack callBack, PreparedStatement pstmt) throws SQLException {
        callBack.callback(pstmt);
        pstmt.executeUpdate();
    }

    private void debugQuery(String sql) {
        log.debug("query : {}", sql);
    }
}
