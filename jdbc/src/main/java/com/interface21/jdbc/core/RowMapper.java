package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {

    T mapForObject(ResultSet rs) throws SQLException;
}
