package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

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
        return execute(sql, pstmt -> {
            preparedStatementSetter.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) throws DataAccessException {
        return execute(con -> {
            try (final PreparedStatement pstmt = con.prepareStatement(sql)) {
                return preparedStatementCallback.doInPreparedStatement(pstmt);
            }
        });
    }

    private <T> T execute(final ConnectionCallBack<T> connectionCallBack) throws DataAccessException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return connectionCallBack.doInConnection(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        return query(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        final List<T> result = queryForList(sql, rowMapper, preparedStatementSetter);
        if (result.isEmpty()) {
            throw new DataAccessException("No results");
        }
        if (result.size() > 1) {
            throw new DataAccessException("Too many results");
        }
        return result.get(0);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return query(sql, rs -> {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        }, args);
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> rse, final Object... args) throws DataAccessException {
        return query(sql, rse, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    private <T> T query(final String sql, ResultSetExtractor<T> rse, PreparedStatementSetter pss) throws DataAccessException {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }
}
