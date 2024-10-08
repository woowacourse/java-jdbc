package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
interface ResultGenerator<T, R> {
    R generate(ResultSetParser<T> parser, ResultSet resultSet) throws SQLException;
}
