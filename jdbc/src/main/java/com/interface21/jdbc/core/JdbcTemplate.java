package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResults(rowMapper, rs);
            }
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("Incorrect result size: expected 1, actual " + results.size());
        }
        return results.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> pstmtExecutor, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            PreparedStatementResolver.setParameters(pstmt, args);

            return pstmtExecutor.apply(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static <T> List<T> mapResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }
}
