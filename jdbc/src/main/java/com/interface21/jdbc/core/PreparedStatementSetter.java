package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 전달받은 문장에 인자를 바인딩합니다. 실행과 자원 정리는 JdbcTemplate이 담당합니다.
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement preparedStatement) throws SQLException;
}
