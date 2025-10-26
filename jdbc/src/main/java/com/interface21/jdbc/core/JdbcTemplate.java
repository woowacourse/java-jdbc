package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcTemplate 클래스는 JDBC 기반의 데이터베이스 접근 로직을 단순화시키는 헬퍼 클래스
 * - 데이터베이스 연결(Connection)과 자원 해제(close) 처리
 *  - SQL 실행 과정에서 발생하는 SQLException을 사용자 정의 예외인 DataAccessException으로 변환하여 반환
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    // DB 커넥션을 얻기 위한 DataSource
    private final DataSource dataSource;

    /**
     * 생성자: JdbcTemplate을 생성할 때 DataSource 주입받기!
     * @param dataSource DB 연결을 관리하는 DataSource 객체
     */
    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * SQL 실행의 핵심 메서드. 자원 관리 관련 중복 부분을 함수형 인터페이스를 이용하여 분리함.
     * - Connection, PreparedStatement를 생성하고, 파라미터를 바인딩한 후 StatementExecutor를 통해 실행한다.
     * - Connection은 트랜잭션 컨텍스트를 고려하여 DataSourceUtils를 통해 관리
     * - PreparedStatement는 자동으로 close
     *
     * @param sql 실행할 SQL문
     * @param args PreparedStatement에 바인딩할 파라미터 배열
     * @param executor PreparedStatement를 실행하는 로직을 가진 함수형 인터페이스
     * @return 제네릭 타입 결과값 (쿼리 결과나 update 결과 등)
     */
    public <R> R execute(String sql, Object[] args, StatementExecutor<R> executor) {
        final Connection conn = getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setPreparedStatementParameter(args, pstmt);
            log.info("query = {}", sql);

            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        } finally {
            com.interface21.jdbc.datasource.DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    /**
     * PreparedStatement에 파라미터를 순서대로 바인딩하는 내부 메서드.
     *
     * @param args SQL ? 에 바인딩할 파라미터
     * @param pstmt PreparedStatement 객체
     * @throws SQLException SQL 실행 중 발생한 예외
     */
    private void setPreparedStatementParameter(Object[] args, PreparedStatement pstmt) throws SQLException {
        final PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
        pss.setValues(pstmt);
    }

    /**
     * INSERT, UPDATE, DELETE 문과 같이 데이터 변경 쿼리를 실행하는 메서드.
     * 내부적으로 execute()를 호출하여 PreparedStatement.executeUpdate()를 수행한다.
     *
     * @param sql 실행할 SQL문
     * @param args SQL에 바인딩할 파라미터
     * @return 영향받은 행(row)의 개수
     */
    public int update(final String sql, final Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    /**
     * SELECT 쿼리 실행 메서드.
     * RowMapper를 사용하여 ResultSet을 도메인 객체로 매핑하고 리스트 형태로 반환한다.
     * queryForObject가 단일 조회라면, query는 객체들을 List로 반환한다.
     *
     * @param sql 실행할 SELECT SQL문
     * @param rowMapper ResultSet → 객체 매핑을 담당하는 RowMapper
     * @param args SQL에 바인딩할 파라미터
     * @return 매핑된 객체 리스트
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, args, pstmt -> getQueryResult(rowMapper, pstmt));
    }

    /**
     * 단일 객체 조회 메서드.
     * query() 실행 후 결과 리스트에서 첫 번째 요소를 반환한다.
     * 결과가 없으면 DataAccessException을 던진다.
     *
     * @param sql 실행할 SELECT SQL문
     * @param rowMapper ResultSet → 객체 매핑을 담당하는 RowMapper
     * @param args SQL에 바인딩할 파라미터
     * @return 매핑된 단일 객체
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> result = execute(sql, args, pstmt -> getQueryResult(rowMapper, pstmt));
        if (result.isEmpty()) {
            throw new DataAccessException("조회 결과가 없습니다.");
        } else if (result.size() > 1) {
            throw new DataAccessException("조회 결과가 2건 이상입니다.");
        }
        return result.getFirst();
    }

    /**
     * ResultSet을 실행하여 RowMapper로 매핑한 결과 리스트를 생성하는 내부 메서드.
     *
     * @param rowMapper ResultSet 한 행을 객체로 변환하는 매퍼
     * @param pstmt 실행할 PreparedStatement
     * @return 매핑된 객체 리스트
     * @throws SQLException SQL 실행 중 발생한 예외
     */
    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs, rs.getRow());
                result.add(object);
            }
            return result;
        }
    }

    /**
     * DataSource에서 데이터베이스 Connection 객체 획득
     * DataSourceUtils를 사용하여 트랜잭션 컨텍스트에서 관리되는 Connection을 재사용
     *
     * @return 데이터베이스에 연결된 Connection 객체
     * @throws DataAccessException 커넥션 획득 과정에서 오류가 발생한 경우
     */
    private Connection getConnection() {
        return com.interface21.jdbc.datasource.DataSourceUtils.getConnection(dataSource);
    }
}
