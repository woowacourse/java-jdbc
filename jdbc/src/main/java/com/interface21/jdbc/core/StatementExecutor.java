package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutor<R> {
    R execute(final PreparedStatement pstmt) throws SQLException;
}

