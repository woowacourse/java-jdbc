package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

interface PreparedStatementCallback<T> {

    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
