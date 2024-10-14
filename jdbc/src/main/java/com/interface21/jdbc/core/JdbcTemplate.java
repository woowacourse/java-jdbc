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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final Connection connection, final String sql, final Object... params) {
        update(connection, sql, pstmt -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        });
    }

    public void update(final String sql, final Object... params) {
        update(sql, pstmt -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        });
    }

    private void update(final Connection connection, final String sql, final PreparedStatementSetter setter) {
        execute(connection, sql, pstmt -> {
            setValues(setter, pstmt);
            return pstmt.executeUpdate();
        });
    }

    private void update(final String sql, final PreparedStatementSetter setter) {
        execute(sql, pstmt -> {
            setValues(setter, pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, rowMapper, pstmt -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        });
    }

    private <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setValues(setter, pstmt);
            return getResult(rowMapper, pstmt);
        });
    }

    public <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return queryForObject(connection, sql, rowMapper, pstmt -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return queryForObject(sql, rowMapper, pstmt -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        });
    }

    private <T> T queryForObject(final Connection connection, final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(connection, sql, pstmt -> {
            setValues(setter, pstmt);
            List<T> result = getResult(rowMapper, pstmt);
            if (result.size() != 1) {
                throw new DataAccessException("result for query have not exactly one");
            }
            return result.getFirst();
        });
    }

    private <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setValues(setter, pstmt);
            List<T> result = getResult(rowMapper, pstmt);
            if (result.size() != 1) {
                throw new DataAccessException("result for query have not exactly one");
            }
            return result.getFirst();
        });
    }

    private <T> T execute(final Connection connection, final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return preparedStatementCallback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return preparedStatementCallback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (!DataSourceUtils.isTransactionalConnection(conn, dataSource)) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rowMapper.mapRow(rs));
            }
            return objects;
        }
    }

    private void setValues(PreparedStatementSetter setter, PreparedStatement pstmt) throws SQLException {
        if (setter != null) {
            setter.setValues(pstmt);
        }
    }
}
