package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcExecution<T> {

    T excute(final PreparedStatement pstm) throws SQLException;
}
