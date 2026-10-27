package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.utils.DataAccessUtils;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(conn -> {
            update(conn, sql, createPreparedStatementSetter(parameters));
            return null;
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return execute(conn -> {
            final List<T> results = query(conn, sql, rm, createPreparedStatementSetter(parameters));
            return DataAccessUtils.nullableSingleResult(results);
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return execute(conn -> query(conn, sql, rm, createPreparedStatementSetter(parameters)));
    }

    private void update(final Connection connection, final String sql, final PreparedStatementSetter pss) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pss.setParameters(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private  <T> List<T> query(final Connection connection, final String sql, final RowMapper<T> rm, final PreparedStatementSetter pss) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pss.setParameters(pstmt);
            return mapResultSetToObject(rm, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapResultSetToObject(final RowMapper<T> rm, final PreparedStatement pstmt) {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rm.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }

    private <T> T execute(final Function<Connection, T> action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return action.apply(conn);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
