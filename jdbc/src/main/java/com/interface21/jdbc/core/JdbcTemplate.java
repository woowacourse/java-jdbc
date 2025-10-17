package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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

    public <T> T select(final String sql, final RowMapper<T> rowMapper, final Object... values) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new EmptyResultDataAccessException("No data found");
                }
                T result = rowMapper.call(rs);
                if (rs.next()) {
                    throw new IncorrectResultSizeDataAccessException("Data size is incorrect");
                }
                return result;
            }
        }, values);
    }

    public <T> List<T> selectList(final String sql, final RowMapper<T> rowMapper, final Object... values) {
        return execute(sql, pstmt -> {
            final List<T> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    T result = rowMapper.call(rs);
                    results.add(result);
                }
            }
            return results;
        }, values);
    }

    public void update(final String sql, final Object... values) {
        execute(sql, PreparedStatement::executeUpdate, values);
    }

    public <T> T execute(final String sql, final JdbcCallback<T> callback, final Object... values) {
        return execute(sql, callback, createPreparedStatementSetter(values));
    }

    private <T> T execute(final String sql, final JdbcCallback<T> callback, final PreparedStatementSetter pss) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return callback.call(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (conn != null && !TransactionSynchronizationManager.hasConnection()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... values) {
        return pstmt -> {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
        };
    }

    private Connection getConnection() throws SQLException {
        if (TransactionSynchronizationManager.hasConnection()) {
            return TransactionSynchronizationManager.getConnection();
        }
        return dataSource.getConnection();
    }
}
