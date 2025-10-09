package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preStmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            initParameters(params, preStmt);
            preStmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void initParameters(Object[] params, PreparedStatement preStmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preStmt.setObject(i + 1, params[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preStmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            initParameters(params, preStmt);
            return executeQuery(rowMapper, preStmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(RowMapper<T> rowMapper, PreparedStatement preStmt) throws SQLException {
        try (ResultSet rs = preStmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }
}
