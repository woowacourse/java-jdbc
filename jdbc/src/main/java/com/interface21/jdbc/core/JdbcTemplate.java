package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ResultSet;
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

    // PreparedStatementSetter 버전
    public int executeUpdate(
            final String sql, final PreparedStatementSetter pstmtSetter
    ) {
        try (var connection = dataSource.getConnection();
             var pstmt = connection.prepareStatement(sql)) {

            pstmtSetter.setValues(pstmt);
            return pstmt.executeUpdate(); // 실행 후 영향을 받은 row 개수 반환
        } catch (SQLException e) {
            throw new DataAccessException("Execute Update Error: " + sql, e);
        }
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
        ResultSet resultSet = null;
        try (var connection = dataSource.getConnection();
             var pstmt = connection.prepareStatement(sql)) {
            var pstmtSetter = ofParams(params);
            pstmtSetter.setValues(pstmt);

            // 쿼리 실행
            resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();

            if (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Execute Query Error");
        } finally {
            closeResultSet(resultSet);
        }
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
     * Result Set 객체 close 메서드
     */
    private void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ignored) {
            throw new RuntimeException("Result Set Close Error");
        }
    }
}
