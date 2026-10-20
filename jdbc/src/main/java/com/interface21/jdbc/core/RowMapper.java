package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper {

    Object mapRow(ResultSet resultSet) throws SQLException;
}
