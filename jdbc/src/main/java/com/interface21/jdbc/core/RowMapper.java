package com.interface21.jdbc.core;

import java.sql.ResultSet;

public interface RowMapper {

    <T> T mapRow(final ResultSet resultSet);
}
