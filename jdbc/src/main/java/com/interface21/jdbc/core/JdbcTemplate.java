package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(Connection conn, String sql, Object... parameters) {
        executeUpdate(conn, sql, parameters, PreparedStatement::executeUpdate);
    }

    private void executeUpdate(Connection conn, String sql, Object[] parameters,
                               ConsumerWrapper<PreparedStatement> execution) {
        try (PreparedStatement pstmt = getPreparedStatement(conn, sql, parameters)) {
            log.debug("query : {}", sql);
            execution.accept(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(conn, sql, parameters, rs -> getInstance(rowMapper, rs));
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, Object... parameters) {
        return executeQuery(conn, sql, parameters, rs -> getInstances(rowMapper, rs));
    }

    private <T> T executeQuery(Connection conn, String sql, Object[] parameters,
                               FunctionWrapper<ResultSet, T> execution) {
        try (PreparedStatement pstmt = getPreparedStatement(conn, sql, parameters);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            return execution.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T getInstance(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return rowMapper.mapRow(rs);
    }

    private <T> List<T> getInstances(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> instances = new ArrayList<>();
        while (rs.next()) {
            instances.add(rowMapper.mapRow(rs));
        }
        return instances;
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql, Object[] parameters)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setParameters(pstmt, parameters);
        return pstmt;
    }

    private void setParameters(PreparedStatement pstmt, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
