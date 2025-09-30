package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    /*
     * 라이브러리 확장
     * RowMapper 인터페이스는 제네릭을 사용해, 캐스팅을 사용하지 않도록 한다.
     */
    T mapRow(ResultSet rs) throws SQLException;
}
