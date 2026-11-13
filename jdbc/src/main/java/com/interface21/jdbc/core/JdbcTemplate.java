package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final void update(String sql, Object... args) {
        update(sql, pstmt -> setValues(pstmt, args));
    }

    public final void update(String sql, PreparedStatementSetter setter) {
        executeWithConnection(conn -> {
            update(sql, conn, setter);
            return null;
        });
    }

    public final void update(String sql, Connection conn, Object... args) {
        update(sql, conn, pstmt -> setValues(pstmt, args));
    }

    public final void update(String sql, Connection conn, PreparedStatementSetter setter) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public final <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, pstmt -> setValues(pstmt, args), rowMapper);
    }

    public final <T> List<T> query(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        return executeWithConnection(conn -> query(sql, conn, setter, rowMapper));
    }

    public final <T> List<T> query(String sql, Connection conn, RowMapper<T> rowMapper, Object... args) {
        return query(sql, conn, pstmt -> setValues(pstmt, args), rowMapper);
    }

    public final <T> List<T> query(String sql, Connection conn, PreparedStatementSetter setter, RowMapper<T> rowMapper) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            try (ResultSet rs = executeQuery(setter, pstmt)) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeWithConnection(Function<Connection, T> action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (ConnectionRelease release = () -> DataSourceUtils.releaseConnection(connection, dataSource)) {
            return action.apply(connection);
        }
    }

    @FunctionalInterface
    private interface ConnectionRelease extends AutoCloseable {

        @Override
        void close();
    }

    private ResultSet executeQuery(PreparedStatementSetter setter, PreparedStatement pstmt) throws SQLException {
        setter.setValues(pstmt);
        return pstmt.executeQuery();
    }

    private void setValues(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
