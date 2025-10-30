package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * SQL을 실행하고 콜백을 통해 결과를 반환한다. (DataSource에서 Connection 획득) DataSourceUtils를 통해 Connection을 획득한다. 트랜잭션 동기화가 활성화되어 있으면
     * 동기화된 Connection을 사용하고, 그렇지 않으면 새로운 Connection을 생성한다.
     */
    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        // DataSourceUtils를 통해 Connection 획득
        // 트랜잭션이 활성화되어 있으면 TransactionSynchronizationManager에 바인딩된 Connection 반환
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (var pstmt = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Execute Error: " + sql, e);
        } finally {
            // Connection 반환
            // 트랜잭션 동기화된 Connection이면 닫지 않고, 일반 Connection이면 닫는다
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * SQL을 실행하고 콜백을 통해 결과를 반환한다. (외부 Connection 사용)
     */
    private <T> T execute(final Connection connection, final String sql, final PreparedStatementCallback<T> callback) {
        try (var pstmt = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Execute Error: " + sql, e);
        }
    }

    /**
     * INSERT, UPDATE, DELETE 같은 데이터 변경 쿼리를 실행한다.
     *
     * @param sql    실행할 SQL 쿼리
     * @param params PreparedStatement에 바인딩할 파라미터
     * @return 영향을 받은 row 개수 INSERT, UPDATE, DELETE, DDL(CREATE TABLE 등) 문을 실행할 때 사용
     */
    // 가변 인자 버전(기존 유지)
    public int executeUpdate(
            final String sql, final Object... params
    ) {
        var pstmtSetter = ofParams(params);
        return executeUpdate(sql, pstmtSetter);
    }

    // 가변 인자 버전 (+ connection)
    public int executeUpdate(
            final Connection connection, final String sql, final Object... params
    ) {
        var pstmtSetter = ofParams(params);
        return executeUpdate(connection, sql, pstmtSetter);
    }

    // PreparedStatementSetter 버전
    public int executeUpdate(
            final String sql, final PreparedStatementSetter pstmtSetter
    ) {
        return execute(sql, pstmt -> {
            pstmtSetter.setValues(pstmt);
            return pstmt.executeUpdate(); // 실행 후 영향을 받은 row 개수 반환
        });
    }

    // PreparedStatementSetter 버전 (+ connection)
    public int executeUpdate(
            final Connection connection, final String sql, final PreparedStatementSetter pstmtSetter
    ) {
        return execute(connection, sql, pstmt -> {
            pstmtSetter.setValues(pstmt);
            return pstmt.executeUpdate(); // 실행 후 영향을 받은 row 개수 반환
        });
    }

    /**
     * SELECT 쿼리를 실행하고 결과를 리스트로 반환한다.
     *
     * @param sql       실행할 SELECT SQL
     * @param rowMapper ResultSet → 객체 매핑 전략
     * @param params    PreparedStatement에 바인딩할 파라미터
     * @return 결과 리스트
     */
    public <T> List<T> executeQuery(
            final String sql, @Nonnull final RowMapper<T> rowMapper, final Object... params
    ) {
        return execute(sql, pstmt -> {
            var pstmtSetter = ofParams(params);
            pstmtSetter.setValues(pstmt);

            // 쿼리 실행 및 결과 처리
            try (var resultSet = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(rowMapper.mapRow(resultSet));
                }
                return result;
            }
        });
    }

    /**
     * 단일 객체를 반환하는 SELECT 쿼리를 실행한다. - 결과가 없으면 예외가 발생한다. - 결과가 2개 이상이면 예외가 발생한다.
     *
     * @param sql       실행할 SELECT SQL
     * @param rowMapper ResultSet → 객체 매핑 전략
     * @param params    PreparedStatement에 바인딩할 파라미터
     * @return 단일 객체
     */
    public <T> T executeQueryForObject(
            final String sql, final RowMapper<T> rowMapper, final Object... params
    ) {
        List<T> result = executeQuery(sql, rowMapper, params);

        if (result.isEmpty()) {
            throw new DataAccessException("Execute Query For Object Error: 쿼리 결과가 비어있습니다.");
        }

        if (result.size() > 1) {
            throw new DataAccessException("Execute Query For Object Error: 쿼리 결과가 2개 이상입니다.");
        }

        return result.getFirst();
    }

    /**
     * 가변 인자 -> PSS 변환 메서드
     */
    private PreparedStatementSetter ofParams(final Object... params) {
        return pstmt -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }

    /**
     * PreparedStatement를 사용하는 콜백 인터페이스
     */
    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
    }
}
