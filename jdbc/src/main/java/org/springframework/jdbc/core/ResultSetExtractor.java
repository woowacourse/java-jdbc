package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public interface ResultSetExtractor<T> {

    T extractData(final ResultSet resultSet) throws SQLException, DataAccessException;
}
