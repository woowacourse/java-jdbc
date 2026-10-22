package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * SQL의 매개변수 값만 바인딩한다. SQL 실행과 JDBC 자원 종료는 JdbcTemplate이 담당한다.
     */
    void setValues(PreparedStatement statement) throws SQLException;
}
