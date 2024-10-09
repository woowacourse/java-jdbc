package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.Nullable;

@FunctionalInterface
public interface PreparedStatementExecutor<T> {

    @Nullable
    T excute(PreparedStatement preparedStatement) throws SQLException;
}
