package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcTemplate 특징 정리
 * <p>
 * Connection 획득: DataSource에서 Connection을 안전하게 획득
 * <p>
 * PreparedStatement 생성: SQL과 매개변수를 이용해 Statement 준비
 * <p>
 * 쿼리 실행: SQL 실행 및 결과 처리
 * <p>
 * ResultSet 매핑: RowMapper를 통해 자바 객체로 변환
 * <p>
 * 예외 변환: SQLException을 Spring의 DataAccessException으로 변환
 * <p>
 * 리소스 정리: Connection, Statement, ResultSet 자동 해제
 */

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(sql, pstmt -> {
            log.debug("query: {}, args: {}", sql, args);

            setParameters(pstmt, args);
            return mapRows(rowMapper, pstmt);
        });
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper)
            throws DataAccessException {
        return execute(sql, pstmt -> {
            log.debug("query: {}, pss: {}", sql, pss);
            pss.setValues(pstmt);

            return mapRows(rowMapper, pstmt);
        });
    }

    private <T> List<T> mapRows(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();

            int rowNum = 0;
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return results;
        }
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(sql, pstmt -> {
            log.debug("queryForObject: {}, args: {}", sql, args);

            setParameters(pstmt, args);
            return extractSingleResult(rowMapper, pstmt);
        });
    }

    private <T> T extractSingleResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }

            T result = rowMapper.mapRow(resultSet, 0);

            validateSingleResult(resultSet);

            return result;
        }
    }

    private void validateSingleResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            int count = 2;
            while (resultSet.next()) {
                count++;
            }
            throw new IncorrectResultSizeDataAccessException(1, count);
        }
    }

    public int update(String sql, Object... args) throws DataAccessException {
        return execute(sql, pstmt -> {
            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
        return execute(sql, pstmt -> {
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        });
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        if (args == null) {
            return;
        }

        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i - 1]);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
