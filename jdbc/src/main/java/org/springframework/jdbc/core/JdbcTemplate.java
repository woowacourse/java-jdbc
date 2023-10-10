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
            setPreparedStatementWithArgs(args, pstmt);
        });
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        return execute(sql, pstmt -> {
            preparedStatementSetter.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        return queryForObject(sql, rowMapper, pstmt -> {
            setPreparedStatementWithArgs(args, pstmt);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        final List<T> result = query(sql, extractList(rowMapper), preparedStatementSetter);
        if (result.isEmpty()) {
            throw new DataAccessException("No results");
        }
        if (result.size() > 1) {
            throw new DataAccessException("Too many results");
        }
        return result.get(0);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return queryForList(sql, rowMapper, pstmt -> {
            setPreparedStatementWithArgs(args, pstmt);
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter preparedStatementSetter) throws DataAccessException {
        return query(sql, extractList(rowMapper), preparedStatementSetter);
    }


    private <T> T query(final String sql, ResultSetExtractor<T> rse, PreparedStatementSetter pss) throws DataAccessException {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) throws DataAccessException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setPreparedStatementWithArgs(final Object[] args, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> ResultSetExtractor<List<T>> extractList(final RowMapper<T> rowMapper) {
        return rs -> {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        };
    }
}
