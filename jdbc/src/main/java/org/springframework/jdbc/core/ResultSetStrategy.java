package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetStrategy<T> {

    public T getData(ResultSet rs) throws SQLException;
}
