package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 현재 행만 변환합니다. 커서 이동과 자원 정리는 JdbcTemplate이 담당합니다. rowNum은 0부터 시작합니다.
 */
@FunctionalInterface
public interface RowMapper<T> {

    T mapRow(ResultSet resultSet, int rowNum) throws SQLException;
}
