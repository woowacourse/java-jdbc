package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultFormatter<T> {

    T format(ResultSet rs) throws SQLException;
}
