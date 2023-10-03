package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    List<T> extractData(ResultSet resultSet) throws SQLException;
}
