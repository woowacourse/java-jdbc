package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... args)  {
        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = getPreparedStatement(conn, sql)) {
            executeSql(args, pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T query(String sql, TransactionalQuery<T> handler, Object ... args) {
        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = getPreparedStatement(conn, sql)) {
            setArgs(args, pstmt);
            try(ResultSet rs = pstmt.executeQuery()) {
                return handler.execute(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
        log.debug("query : {}", sql);
        return conn.prepareStatement(sql);
    }

    private void executeSql(Object[] args, PreparedStatement pstmt) throws SQLException {
        setArgs(args, pstmt);
        pstmt.executeUpdate();
    }

    private void setArgs(Object[] args, PreparedStatement pstmt) throws SQLException {
        for(int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
