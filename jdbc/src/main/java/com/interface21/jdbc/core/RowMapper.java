package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 현재 행을 객체로 변환한다. 행 이동과 자원 정리는 호출자가 담당한다.
     */
    T mapRow(ResultSet resultSet) throws SQLException;
}
