package com.interface21.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper {

    <T> T rowMap(ResultSet rs, int rowNumber) throws SQLException;
}
