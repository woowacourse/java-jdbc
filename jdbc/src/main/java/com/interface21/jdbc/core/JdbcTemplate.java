package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, statement -> bindParameters(statement, args), PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> bindParameters(statement, args), statement -> {
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        });
    }

    /**
     * SQL을 실행해 최대 한 행을 객체로 매핑한다.
     *
     * @param sql 실행할 SQL
     * @param rowMapper 조회 결과 한 행을 객체로 변환하는 매퍼
     * @param args SQL에 선언된 순서대로 바인딩할 매개변수
     * @param <T> 조회 결과의 타입
     * @return 매핑한 객체. 조회 결과가 없으면 {@code null}
     * @throws DataAccessException 조회 결과가 두 행 이상이거나 SQL 처리 중 오류가 발생한 경우
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row but found " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter statementSetter,
                          final PreparedStatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            statementSetter.setValues(statement);
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void bindParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
