package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        if (results.isEmpty()) {
            throw new EmptyDataAccessException();
        }

        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeInternal(sql, pstmt -> {
            final ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }

            return results;
        }, args);
    }

    private <T> T executeInternal(final String sql, final QueryExecutor<T> executor, final Object... args) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            return executeInternalWithConnection(dataSource.getConnection(), sql, executor, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private <T> T executeInternalWithConnection(final Connection con, final String sql, final QueryExecutor<T> executor, final Object... args) {
        try (final PreparedStatement pstmt = con.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParamsToPreparedStatement(pstmt, args);

            return executor.run(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParamsToPreparedStatement(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public int update(final String sql, final Object... args) {
        return executeInternal(sql, PreparedStatement::executeUpdate, args);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        return executeInternalWithConnection(connection, sql, PreparedStatement::executeUpdate, args);
    }
}
