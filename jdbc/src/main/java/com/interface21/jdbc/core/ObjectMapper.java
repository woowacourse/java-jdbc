package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ObjectMapper <T> {

    T map(ResultSet resultSet, int rowNum) throws SQLException;
}
