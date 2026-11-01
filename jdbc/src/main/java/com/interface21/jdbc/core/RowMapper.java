package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/** 현재 행의 값만 변환한다. 커서 이동과 자원 정리는 {@link JdbcTemplate}이 담당한다. */
@FunctionalInterface
public interface RowMapper<T> {

  T mapRow(ResultSet resultSet) throws SQLException;
}
