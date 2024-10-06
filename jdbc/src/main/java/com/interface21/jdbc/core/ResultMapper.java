package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultMapper<T> {

    T mapResult(ResultSet resultSet) throws SQLException;
}
