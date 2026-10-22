package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 현재 행을 변환한다. 다음 행으로의 이동은 JdbcTemplate이 담당한다.
     */
    T mapRow(ResultSet resultSet) throws SQLException;
}
