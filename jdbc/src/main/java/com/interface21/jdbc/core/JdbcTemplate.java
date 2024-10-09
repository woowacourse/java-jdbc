package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.DataAccessUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public <T> List<T> query(String sql, ParameterSetter parameterSetter, ResultSetExtractor<T> resultExtractor, Object... args) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return resultExtractor.extractResults(rs);
            }
        }, parameterSetter, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new DefaultParameterSetter(), new DefaultResultSetExtractor<>(rowMapper), args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, ParameterSetter parameterSetter, Object... args) {
        return query(sql, parameterSetter, new DefaultResultSetExtractor<>(rowMapper), args);
    }

    public <T> List<T> query(String sql, ResultSetExtractor<T> resultExtractor, Object... args) {
        return query(sql, new DefaultParameterSetter(), resultExtractor, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> T queryForObject(String sql, ParameterSetter parameterSetter, ResultSetExtractor<T> resultExtractor, Object... args) {
        List<T> results = query(sql, parameterSetter, resultExtractor, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, ParameterSetter parameterSetter, Object... args) {
        List<T> results = query(sql, rowMapper, parameterSetter, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> T queryForObject(String sql, ResultSetExtractor<T> resultExtractor, Object... args) {
        List<T> results = query(sql, new DefaultParameterSetter(), resultExtractor, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public int update(String sql, ParameterSetter parameterSetter, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, parameterSetter, args);
    }

    public int update(String sql, Object... args) {
        return update(sql, new DefaultParameterSetter(), args);
    }

    private <T> T execute(String sql, StatementCallback<T> action, ParameterSetter parameterSetter, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            parameterSetter.setParameters(pstmt, args);
            return action.doInStatement(pstmt);
        } catch (SQLException exception) {
            log.error("쿼리 실행 중 에러가 발생했습니다.", exception);
            throw new DataAccessException("쿼리 실행 에러 발생", exception);
        }
    }
}
