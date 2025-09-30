package com.interface21.jdbc.core;

import java.sql.ResultSet;

public interface RowMapper<T> {

    T map(ResultSet resultSet);
}
