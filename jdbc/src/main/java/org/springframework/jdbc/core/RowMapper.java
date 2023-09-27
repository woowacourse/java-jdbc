package org.springframework.jdbc.core;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet resultSet);
}
