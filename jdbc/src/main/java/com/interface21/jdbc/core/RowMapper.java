package com.interface21.jdbc.core;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T mapRow(ResultSet resultSet) throws Exception;
}
