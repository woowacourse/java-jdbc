package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public <T> T selectOne(final RowMapper<T> rowMapper, final String sql, final Object... args) {
        return selectOne(rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> T selectOne(final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<T> stmtExecutor = pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapSingleRow(rowMapper, rs);
            }
        };

        return execute(stmtExecutor, sql, pss);
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

    public <T> List<T> selectMulti(final RowMapper<T> rowMapper, final String sql, final PreparedStatementSetter pss) {
        StatementExecutor<List<T>> stmtExecutor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                return mapMultipleRows(rowMapper, rs);
            }
        };

        return execute(stmtExecutor, sql, pss);
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
        // 트랜잭션 동기화가 되지 않고 있는 상태라면, 즉 트랜잭션이 이 메서드에서 열렸다면 커넥션을 반납합니다.
        // 만약 동기화되고 있는 상태라면, TransactionExecutor가 커넥션을 반납하기를 기대합니다.
        boolean isTxSynchronized = DataSourceUtils.isSynchronizedWithTransaction();

        final Connection connection = DataSourceUtils.getConnection(dataSource);
        T result = execute(connection, stmtExecutor, sql, pss);
        if (!isTxSynchronized) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        return result;
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
