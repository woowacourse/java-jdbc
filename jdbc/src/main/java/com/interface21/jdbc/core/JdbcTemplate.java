package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(Connection connection, String sql, PreparedStatementSetter pss) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public int update(String sql, Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), pss);
    }

    public <T> T query(String sql, ResultSetExtractor<T> rse, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rse.extractData(rs);
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementCallBack<T> callBack) {
        validateSql(sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return callBack.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void validateSql(String sql) {
        if (sql.isBlank()) {
            throw new IllegalArgumentException("sql must not be blank");
        }
        log.debug("Executing prepared SQL statement : [ {} ]", sql);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> results = query(sql, rowMapper, pss);
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(results.getFirst());
    }
}
