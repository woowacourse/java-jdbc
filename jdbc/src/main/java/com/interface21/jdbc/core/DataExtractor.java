package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
interface DataExtractor<T, R> {

    R extract(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException;
}
