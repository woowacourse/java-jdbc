package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;
}
