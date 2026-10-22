package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper{
    Object mapRow(ResultSet rs) throws SQLException;
}
