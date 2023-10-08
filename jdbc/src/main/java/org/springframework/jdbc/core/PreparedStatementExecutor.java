package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementExecutor<T> {

    public T execute(final PreparedStatement pst) throws SQLException;
}
