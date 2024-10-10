package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionalCallback {

    void executeAsTransactional(Connection connection) throws SQLException, DataAccessException;
}
