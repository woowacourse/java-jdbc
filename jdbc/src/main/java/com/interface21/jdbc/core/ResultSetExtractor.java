package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    List<T> extractResults(ResultSet rs) throws SQLException;
}
