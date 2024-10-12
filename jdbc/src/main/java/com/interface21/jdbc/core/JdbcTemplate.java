package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        return execute(sql, args, pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            List<T> queryResult = new ArrayList<>();
            while (rs.next()) {
                queryResult.add(rowMapper.mapRow(rs));
            }
            return queryResult;
        });
    }

    private <T> T execute(String sql, Object[] args, PreparedStatementCallback<T> callback) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("Executing SQL: {}", sql);
            setParameter(pstmt, args);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        List<T> queryResult = query(sql, rowMapper, args);
        if (queryResult.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, queryResult.size());
        }
        if (queryResult.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        return queryResult.getFirst();
    }

    public int update(final String sql, Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    public int update(final String sql, GeneratedKeyHolder keyHolder, Object... args) {
        return execute(sql, args, pstmt -> {
            ResultSet rs = pstmt.getGeneratedKeys();
            int affectedRows = pstmt.executeUpdate();
            if (rs.next()) {
                keyHolder.setKey(rs.getObject(1));
            }
            return affectedRows;
        });
    }

    private void setParameter(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
