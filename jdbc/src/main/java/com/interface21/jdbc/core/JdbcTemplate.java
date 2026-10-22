package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ... args) {
        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = getPreparedStatement(conn, sql)) {
            setArgs(args, pstmt);
            try(ResultSet rs = pstmt.executeQuery()) {
                return mapRows(rs, rowMapper);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
        log.debug("query : {}", sql);
        return conn.prepareStatement(sql);
    }

    private <T> List<T> mapRows(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while(rs.next()){
            results.add(rowMapper.mapRow(rs));
        }
        return results;
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
