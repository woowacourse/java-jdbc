package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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

    public int update(String sql, Object... args) {
        boolean ownsConnection = TransactionSynchronizationManager.getResource(dataSource) == null;

        Connection conn = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)){

            bindParams(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (ownsConnection) {
                try {
                    DataSourceUtils.releaseConnection(conn, dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {

        List<T> list = new ArrayList<>();

        boolean ownsConnection = TransactionSynchronizationManager.getResource(dataSource) == null;

        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)
            ){

            log.debug("query: {}", sql);

            bindParams(pstmt, args);

            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    list.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            if (ownsConnection) {
                try {
                    DataSourceUtils.releaseConnection(conn, dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }
        }
        return list;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {

        List<T> result = query(sql, rowMapper, args);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private void bindParams(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i+1, args[i]);
        }
    }
}
