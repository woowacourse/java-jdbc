package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet resultSet) throws SQLException, DataAccessException;
}
