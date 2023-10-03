package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SelectExecutor<T> {

    T execute(ResultSet resultSet) throws SQLException;

}
