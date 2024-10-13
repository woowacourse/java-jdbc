package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.util.function.Function;

@FunctionalInterface
public interface ResultSetMapper<T> extends Function<ResultSet, T> {
}
