package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator();
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryExecutor(sql, (pstmt) -> getSingleQueryResult(rowMapper, pstmt), args);
    }

    private <T> T queryExecutor(String sql, SqlExecutor<T> executor, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn, sql, args)
        ) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<T> getSingleQueryResult(
            final RowMapper<T> rowMapper,
            final PreparedStatement pstmt
    ) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            if (rs.last()) {
                validateSingleRow(rs);
                return Optional.of(rowMapper.mapRow(rs));
            }
            return Optional.empty();
        }
    }

    private void validateSingleRow(final ResultSet rs) throws SQLException {
        if (rs.getRow() != 1) {
            throw new IllegalArgumentException("조회 결과가 2개 이상입니다.");
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return queryExecutor(sql, (pstmt) -> getMultipleQueryResult(rowMapper, pstmt), args);
    }

    private <T> List<T> getMultipleQueryResult(
            final RowMapper<T> rowMapper,
            final PreparedStatement pstmt
    ) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    public int execute(final String sql, final Object... args) {
        return queryExecutor(sql, PreparedStatement::executeUpdate, args);
    }

}
