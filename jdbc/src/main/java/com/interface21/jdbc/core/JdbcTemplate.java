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

    public JdbcTemplate(final DataSource dataSource) throws DataAccessException {
        this.dataSource = dataSource;
        testConnection(dataSource);
    }

    public void update(final String sql, final Object... args) {
        update(sql, PreparedStatementSetter.ofSequenced(args));
    }

    public void update(final String sql, final PreparedStatementSetter pss) {
        execute(PreparedStatement::execute, sql, pss);
    }

    public void update(final Connection conn, final String sql, final Object... args) {
        update(conn, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public void update(final Connection conn, final String sql, final PreparedStatementSetter pss) {
        execute(conn, PreparedStatement::execute, sql, pss);
    }

    public <T> T selectOne(final RowMapper<T> rowMapper, final String sql, final Object... args) {
        return selectOne(rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> T selectOne(final Connection conn, final RowMapper<T> rowMapper, final String sql, final Object... args) {
        return selectOne(conn, rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> T selectOne(final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<T> stmtExecutor = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapSingleRow(rowMapper, rs);
            }
        };

        return execute(stmtExecutor, sql, pss);
    }

    public <T> T selectOne(final Connection conn, final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<T> stmtExecutor = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapSingleRow(rowMapper, rs);
            }
        };

        return execute(conn, stmtExecutor, sql, pss);
    }

    private <T> T mapSingleRow(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rowMapper.mapRow(rs);
        }
        return null;
    }

    public <T> List<T> selectMulti(final RowMapper<T> rowMapper, final String sql, final Object... args) {
        return selectMulti(rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> List<T> selectMulti(final Connection conn, final RowMapper<T> rowMapper, final String sql, final Object... args) {
        return selectMulti(conn, rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> List<T> selectMulti(final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<List<T>> stmtExecutor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                return mapMultipleRows(rowMapper, rs);
            }
        };

        return execute(stmtExecutor, sql, pss);
    }

    public <T> List<T> selectMulti(final Connection conn, final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<List<T>> stmtExecutor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                return mapMultipleRows(rowMapper, rs);
            }
        };

        return execute(conn, stmtExecutor, sql, pss);
    }

    private <T> List<T> mapMultipleRows(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        var list = new ArrayList<T>();
        while (rs.next()) {
            var mappedRow = rowMapper.mapRow(rs);
            list.add(mappedRow);
        }
        return list;
    }

    private <T> T execute(final StatementExecutor<T> stmtExecutor, final String sql, final PreparedStatementSetter pss) {
        try (final Connection connection = dataSource.getConnection()) {
            return execute(connection, stmtExecutor, sql, pss);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection connection, final StatementExecutor<T> stmtExecutor, final String sql, final PreparedStatementSetter pss) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return stmtExecutor.execute(pstmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void testConnection(final DataSource dataSource) {
        try (final Connection connection = dataSource.getConnection()) {
            var databaseProductName = connection.getMetaData().getDatabaseProductName();
            log.info("Connection established to database : {}", databaseProductName);

        } catch (final NullPointerException e) {
            log.error("Connection is null on dataSource {}", dataSource);
            throw new DataAccessException(e);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e.getCause());
            throw new DataAccessException(e);
        }
    }
}
