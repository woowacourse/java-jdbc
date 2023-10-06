package org.springframework.jdbc.core;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    @Nullable
    T mapRow(ResultSet rs) throws SQLException;
}
