package org.springframework.jdbc.core;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface QueryExecutor<T> {

     T run(final PreparedStatement pstmt);
}
