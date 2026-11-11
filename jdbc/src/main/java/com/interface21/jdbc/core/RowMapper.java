package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 현재 행을 객체로 변환한다. 커서를 이동하거나 자원을 해제하지 않는다.
     */
    T mapRow(ResultSet resultSet) throws SQLException;
}
