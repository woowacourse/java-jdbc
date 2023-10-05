package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface MyRowMapper<T> {

    @Nonnull
    T map(ResultSet rs) throws SQLException;
}
