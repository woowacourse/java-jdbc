package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryCallback<T> {

    T call(PreparedStatement preparedStatement) throws SQLException;
}
