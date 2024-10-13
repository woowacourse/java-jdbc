package com.interface21.jdbc.core.mapper;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T mapping(final ResultSet resultSet);
}
