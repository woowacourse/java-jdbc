package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(
        final String sql,
        final RowMapper<T> rowMapper,
        final Object... args
    ) {
        PreparedStatementFunction<T> pstmtFunction = pstmt -> {
            return mapSingleResult(rowMapper, pstmt);
        };
        return execute(sql, pstmtFunction, args);
    }

    private <T> T mapSingleResult(RowMapper<T> rowMapper, PreparedStatement pstmt) {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            if (!resultSet.next()) { // 쿼리 결과 없음
                return null;
            }
            T returnValue = rowMapper.mapRow(resultSet);
            if (resultSet.next()) { // 결과 1개 초과
                throw new DataAccessException("query returns more than one row");
            }
            return returnValue;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        PreparedStatementFunction<List<T>> pstmtFunction = pstmt -> {
            return mapResults(rowMapper, pstmt);
        };
        return execute(sql, pstmtFunction, args);
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, PreparedStatement pstmt) {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public int update(Connection connection, String sql, Object... args) {
        return execute(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(
        Connection connection,
        String sql,
        PreparedStatementFunction<T> pstmtFunction,
        Object... args
    ) {
        try (PreparedStatement pstmt = getPreparedStatementWithArguments(connection, sql, args)) {
            log.debug("query : {}", sql);
            return pstmtFunction.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(String sql, PreparedStatementFunction<T> pstmtFunction, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatementWithArguments(conn, sql, args);
        ) {
            log.debug("query : {}", sql);
            return pstmtFunction.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getPreparedStatementWithArguments(
        final Connection conn,
        final String sql,
        final Object... args
    ) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i =0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
