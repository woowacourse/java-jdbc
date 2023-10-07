package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public int update(final String sql, final Object... args) throws DataAccessException {
        return update(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        try {
            return execute(sql, pstmt -> {
                preparedStatementSetter.setValues(pstmt);
                return pstmt.executeUpdate();
            });
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) throws SQLException {
        return execute(connection -> {
            try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
                return preparedStatementCallback.doInPreparedStatement(pstmt);
            }
        });
    }

    private <T> T execute(final ConnectionCallBack<T> connectionCallBack) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            return connectionCallBack.doInConnection(connection);
        }
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        return query(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) {
        try {
            return query(sql, rs -> {
                if (rs.next()) {
                    return rowMapper.mapRow(rs, rs.getRow());
                }
                throw new SQLException("No data found");
            }, preparedStatementSetter);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T query(String sql, ResultSetExtractor<T> rse, PreparedStatementSetter pss) throws SQLException {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        try {
            return query(sql, rs -> {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rs.getRow()));
                }
                return results;
            }, args);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> rse, final Object... args) throws SQLException {
        return query(sql, rse, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

}
