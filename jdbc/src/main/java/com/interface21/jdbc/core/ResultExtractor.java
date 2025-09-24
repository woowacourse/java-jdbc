package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultExtractor<T> {

    T extract(ResultSet resultSet) throws SQLException;
}
