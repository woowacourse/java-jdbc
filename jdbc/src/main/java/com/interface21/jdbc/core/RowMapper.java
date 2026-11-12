package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link ResultSet}의 현재 행을 객체로 변환한다.
 * <p>
 * 행 순회와 {@link ResultSet} 자원 관리는 {@link JdbcTemplate}이 소유한다.
 * 구현체는 현재 행만 변환해야 하며, {@code next()} 같은 커서 이동 메서드를 호출하거나
 * {@link ResultSet}을 닫아서는 안 된다.
 *
 * @param <T> 행을 변환한 결과 타입
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 현재 행을 객체로 변환한다.
     *
     * @param resultSet 커서가 변환할 행을 가리키고 있는 결과 집합
     * @param rowNum    0부터 시작하는 행 번호
     * @return 변환된 객체
     * @throws SQLException 컬럼을 읽다가 실패한 경우. {@link JdbcTemplate}이 {@code DataAccessException}으로 변환한다.
     */
    T mapRow(ResultSet resultSet, int rowNum) throws SQLException;
}
