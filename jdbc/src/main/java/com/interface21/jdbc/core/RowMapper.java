package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;

@FunctionalInterface
public interface RowMapper<T> {

    @Nullable
    T mapRow(final ResultSet rs, final int rowNum) throws SQLException;
}
