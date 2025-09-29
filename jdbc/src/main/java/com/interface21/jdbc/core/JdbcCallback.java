package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcCallback<T> {

    T call(ResultSet rs)  throws SQLException;
}
