package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 현재 행을 객체로 변환한다. 커서 이동과 자원 해제는 JdbcTemplate이 담당한다.
     */
    T mapRow(ResultSet resultSet) throws SQLException;
}
