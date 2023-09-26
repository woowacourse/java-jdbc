package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;

@FunctionalInterface
public interface RowMapper<T> {

    @Nullable
    T mapRow(ResultSet resultSet, int rowNumber) throws SQLException;
}
