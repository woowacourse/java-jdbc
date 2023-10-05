package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

interface PreparedStatementExecutor<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;
}
