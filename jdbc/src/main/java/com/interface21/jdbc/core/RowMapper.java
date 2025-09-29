package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface RowMapper<T> {

    T mapForObject(ResultSet rs) throws SQLException;

    List<T> mapForObjects(ResultSet rs) throws SQLException;
}
