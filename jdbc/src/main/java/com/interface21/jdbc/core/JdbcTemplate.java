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

    private static final int PREPARED_STATEMENT_INDEX_OFFSET = 1;
    private static final int SINGLE_OBJECT_COUNT = 1;
    private static final int EMPTY_SIZE = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql) {
        return executeWithoutConnection(conn -> update(conn, sql));
    }

    public int update(String sql, Object... args) {
        return executeWithoutConnection(conn -> update(conn, sql, args));
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return executeWithoutConnection(conn -> update(conn, sql, pss));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeWithoutConnection(conn -> query(conn, sql, rowMapper, args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return executeWithoutConnection(conn -> query(conn, sql, pss, rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeWithoutConnection(conn -> queryForObject(conn, sql, rowMapper, args));
    }

    public int update(Connection conn, String sql) {
        return update(conn, sql, new Object[EMPTY_SIZE]);
    }

    public int update(Connection conn, String sql, Object... args) {
        return execute(conn, sql, PreparedStatement::executeUpdate, args);
    }

    public int update(Connection conn, String sql, PreparedStatementSetter pss) {
        return execute(conn, sql, PreparedStatement::executeUpdate, pss);
    }

    public <T> List<T> query(Connection conn, String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(conn, sql, pstmt -> extractResults(rowMapper, pstmt.executeQuery()), pss);
    }

    public <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(conn, sql, pstmt -> extractResults(rowMapper, pstmt.executeQuery()), args);
    }

    public <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(conn, sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new DataAccessException("찾으려는 값이 존재하지 않습니다.");
        }
        if (results.size() > SINGLE_OBJECT_COUNT) {
            throw new DataAccessException("조회 결과 개수가 2 이상입니다. actual " + results.size());
        }
        return results.getFirst();
    }

    private <T> T executeWithoutConnection(ConnectionCallback<T> callback) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        return callback.doInConnection(conn);
    }

    private <T> T execute(
            Connection conn, String sql, PreparedStatementCallback<T> callback, PreparedStatementSetter setter
    ) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementCallback<T> callback, Object... args) {
        try (PreparedStatement pstmt = createPreparedStatement(conn, sql, args)) {
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setArguments(pstmt, args);
        return pstmt;
    }

    private void setArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int paramIndex = 0; paramIndex < args.length; paramIndex++) {
            pstmt.setObject(paramIndex + PREPARED_STATEMENT_INDEX_OFFSET, args[paramIndex]);
        }
    }

    private <T> List<T> extractResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }
}
