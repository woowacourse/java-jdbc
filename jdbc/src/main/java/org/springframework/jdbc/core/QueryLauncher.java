package org.springframework.jdbc.core;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface QueryLauncher<T> {

    T execute(final PreparedStatement pstmt);
}
