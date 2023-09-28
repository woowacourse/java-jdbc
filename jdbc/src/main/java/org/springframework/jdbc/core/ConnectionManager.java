package org.springframework.jdbc.core;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.sql.Connection;

public interface ConnectionManager {

    Connection getConnection() throws CannotGetJdbcConnectionException;

}
