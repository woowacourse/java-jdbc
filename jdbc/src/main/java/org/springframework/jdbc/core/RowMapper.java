package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface RowMapper<T> {

    @Nonnull
    T mapRow(final ResultSet resultSet, final int rowNumber) throws SQLException;
}

