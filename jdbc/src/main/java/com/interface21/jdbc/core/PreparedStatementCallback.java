package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nullable;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
