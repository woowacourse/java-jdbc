package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PreparedStatement의 파라미터 값을 설정하는 콜백 인터페이스
 * <p>
 * PreparedStatement에 파라미터를 바인딩하는 로직 캡슐화
 * SQLException 처리는 JdbcTemplate에서 담당하므로 구현체는 파라미터 설정에만 집중할 수 있습니다.
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * PreparedStatement에 파라미터 값을 설정합니다.
     *
     * @param pstmt 파라미터를 설정할 PreparedStatement
     * @throws SQLException SQL 예외 발생 시
     */
    void setValues(PreparedStatement pstmt) throws SQLException;
}
