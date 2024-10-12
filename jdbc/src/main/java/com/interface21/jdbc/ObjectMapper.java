package com.interface21.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ObjectMapper<T>  {

    T mapToObject(ResultSet rs) throws SQLException;
}
