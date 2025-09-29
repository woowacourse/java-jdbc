package com.interface21.jdbc.core;

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

    private static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ignored) {
            throw new RuntimeException("Result Set Close Error");
        }
    }

    /**
     * INSERT, UPDATE, DELETE 같은 데이터 변경 쿼리를 실행한다.
     *
     * @param sql    실행할 SQL 쿼리
     * @param params PreparedStatement에 바인딩할 파라미터
     * @return 영향을 받은 row 개수 INSERT, UPDATE, DELETE, DDL(CREATE TABLE 등) 문을 실행할 때 사용
     */
    public int executeUpdate(
            final String sql, final Object... params
    ) {
        try (var connection = dataSource.getConnection();
             var pstmt = connection.prepareStatement(sql)) {

            for (int i = 1; i <= params.length; i++) { // SQL 파라미터 바인딩
                pstmt.setObject(i, params[i - 1]);
            }

            return pstmt.executeUpdate(); // 실행 후 영향을 받은 row 개수 반환
        } catch (SQLException e) {
            throw new RuntimeException("Execute Update Error");
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

            for (int i = 1; i <= params.length; i++) { // SQL 파라미터 바인딩
                pstmt.setObject(i, params[i - 1]);
            }

            // 쿼리 실행
            resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();

            if (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Execute Query Error");
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
            throw new RuntimeException("Execute Query For Object Error: 쿼리 결과가 비어있습니다.");
        }

        if (result.size() > 1) {
            throw new RuntimeException("Execute Query For Object Error: 쿼리 결과가 2개 이상입니다.");
        }

        return result.getFirst();
    }
}
