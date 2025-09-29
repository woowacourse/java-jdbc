package com.interface21.jdbc.core;

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
 * Java Database Connectivity Template
 * <p>
 * 데이터베이스 연결, PreparedStatement 생성, 파라미터 설정, 결과 처리, 리소스 해제 등의 반복적인 JDBC 작업을 자동화하여 코드 중복을 제거합니다.
 * </p>
 *
 * <p>
 * JDBC 핵심 구성요소:
 * <ul>
 *   <li>DataSource: 커넥션 풀을 통한 데이터베이스 연결 관리</li>
 *   <li>Connection: 데이터베이스 연결</li>
 *   <li>Statement/PreparedStatement: SQL 실행</li>
 *   <li>ResultSet: 조회 결과 처리</li>
 * </ul>
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // insert, update, delete
    public void update(final String sql, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    // 단일 객체 조회
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // 객체 리스트 조회
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            // SQL 문을 미리 컴파일해서 PreparedStatement 객체 생성
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            // 쿼리 실행 결과(ResultSet) 반환
            rs = pstmt.executeQuery();

            List<T> results = new ArrayList<>();
            // rs.next(): 다음 ResultSet이 있으면 true, 없으면 false 반환. 커서를 다음 행으로 이동.
            while (rs.next()) {
                T result = rowMapper.mapRow(rs);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            // SQL의 ?(placeholder)에 실제 값을 대입
            // SQL Injection 공격 방지
            pstmt.setObject(i + 1, args[i]);
        }
    }

    // JDBC 리소스들 정리
    // ResultSet, PreparedStatement, Connection을 역순으로 해제
    // conn.close()만 성공해도 내부적으로 ResultSet, PreparedStatement도 자동 해제되지만, 역순 해제는 좋은 관습임. 생성 순서를 기억하고 있다는 의미.
    private void closeResources(final ResultSet rs, final PreparedStatement pstmt, final Connection conn) {
        try {
            if (rs != null) {
                // ResultSet 해제. 메모리 절약.
                rs.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            if (pstmt != null) {
                // PreparedStatement 해제. 메모리 절약.
                pstmt.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            // Connection 반환을 위해 Connection 해제
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
