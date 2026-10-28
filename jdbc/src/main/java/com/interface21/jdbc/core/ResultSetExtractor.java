package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 첫 행 이전에 위치한 결과 집합을 전달받아 순회하고 추출합니다. 자원 정리는 JdbcTemplate이 담당합니다.
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet resultSet) throws SQLException;
}
