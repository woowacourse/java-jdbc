package org.springframework.jdbc.core;

import java.sql.Connection;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public interface ConnectionManager {

    Connection getConnection() throws CannotGetJdbcConnectionException;

}
