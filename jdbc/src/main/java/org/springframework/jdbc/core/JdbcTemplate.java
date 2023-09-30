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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator();
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery()
        ) {
            if (rs.last()) {
                validateSingleRow(rs);
                return rowMapper.mapRow(rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private void validateSingleRow(final ResultSet rs) throws SQLException {
        if (rs.getRow() != 1) {
            throw new IllegalArgumentException("조회 결과가 2개 이상입니다.");
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery()
        ) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void execute(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn, sql, args)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
