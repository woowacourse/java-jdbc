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
        return executeQuery(sql, pstmt -> SingleResult.from(getQueryResult(rowMapper, pstmt)), args);
    }

    private <T> T executeQuery(final String sql, final SqlExecutor<T> executor, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn, sql, args)
        ) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getQueryResult(
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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, pstmt -> getQueryResult(rowMapper, pstmt), args);
    }

    public int execute(final String sql, final Object... args) {
        return executeQuery(sql, PreparedStatement::executeUpdate, args);
    }

}
