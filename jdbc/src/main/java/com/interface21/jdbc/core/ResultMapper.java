package com.interface21.jdbc.core;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultMapper<T> {

    T mapResult(ResultSet resultSet);
}
