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
import org.springframework.jdbc.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int update(String sql, Object... args) throws DataAccessException {
        return execute(sql, (pstmt) -> {
            prepareStatement(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args)
            throws DataAccessException {
        return execute(sql, (pstmt) -> {
            prepareStatement(pstmt, args);
            try (ResultSet rs = pstmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                if (rs.first() && rs.isLast()) {
                    Object[] initArgs = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        initArgs[i - 1] = rs.getObject(i);
                    }
                    return InstantiateUtil.instantiate(rs, requiredType, initArgs);
                }
                throw new IncorrectResultSizeDataAccessException();
            }
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        return execute(sql, (pstmt) -> {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, 0));
                }
                return results;
            }
        });
    }

    private void prepareStatement(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i - 1]);
        }
    }

    private <T> T execute(String sql, StatementExecution<PreparedStatement, T> function) {
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            return function.apply(pstmt);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
